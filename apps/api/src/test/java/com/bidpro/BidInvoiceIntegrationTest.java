package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BidInvoiceIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao users;
    @Autowired CategoryDao categories;
    @Autowired ProductDao products;
    @Autowired AuctionDao auctions;
    @Autowired BidDao bids;
    @Autowired InvoiceDao invoices;
    @Autowired LogDao audit;
    @Autowired JwtUtil jwt;
    User seller;
    User bidder;
    User outsider;
    User admin;
    Category category;
    Product product;

    @BeforeEach
    void fixture() {
        seller = user(User.Role.BOTH);
        bidder = user(User.Role.BIDDER);
        outsider = user(User.Role.BIDDER);
        admin = user(User.Role.ADMIN);
        category = new Category();
        category.setCategoryID(id());
        category.setName("Domain test " + id().substring(0, 8));
        categories.saveAndFlush(category);
        product = product(LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusMinutes(5));
    }

    @Test
    @Transactional
    void bidsUseServerIdentityAndEnforceMinimumIncrease() throws Exception {
        mvc.perform(post("/api/bids").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productID", product.getProductID(), "amount", 100))))
                .andExpect(status().isUnauthorized());
        JsonNode first = bid(bidder, product, 100).get("data");
        assertEquals(bidder.getUserID(), first.get("bidderID").asText());
        assertNotNull(first.get("timestamp"));
        mvc.perform(post("/api/bids").header("Authorization", token(bidder)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productID", product.getProductID(), "amount", 105))))
                .andExpect(status().isBadRequest());
        JsonNode increase = data(mvc.perform(put("/api/bids/" + first.get("bidID").asText()).header("Authorization", token(bidder))
                .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":110,\"bidderID\":\"forged\",\"timestamp\":\"2001-01-01T00:00:00\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.bidderID").value(bidder.getUserID()))
                .andExpect(jsonPath("$.data.amount").value(110)).andReturn());
        assertNotEquals(first.get("bidID").asText(), increase.get("bidID").asText(), "An increase appends a new acceptance");
        Bid original = bids.findById(first.get("bidID").asText()).orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(original.getAmount()), "Accepted amounts cannot be rewritten");
        assertEquals(2, bids.findByProductIDOrderByAmountDescTimestampAsc(product.getProductID()).size());
        assertEquals("ACCEPTED", audit.findByEntityTypeAndEntityID("BID", original.getBidID()).get(0).getOperation());
        assertEquals(bidder.getUserID(), audit.findByEntityTypeAndEntityID("BID", increase.get("bidID").asText()).get(0).getPerformedBy());
        mvc.perform(get("/api/products/" + product.getProductID()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.bidCount").value(2))
                .andExpect(jsonPath("$.data.currentBid").value(110));
        mvc.perform(get("/api/bids/mine").header("Authorization", token(bidder)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].productTitle").value(product.getTitle()));
        mvc.perform(get("/api/bids/bidder/" + bidder.getUserID()).header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());
        String publicHistory = mvc.perform(get("/api/bids/product/" + product.getProductID())).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(publicHistory.contains(bidder.getEmail()), "Public history must not disclose bidder emails");
    }

    @Test
    @Transactional
    void biddersCannotChangeSomeoneElsesBidOrProduct() throws Exception {
        String bidID = bid(bidder, product, 100).get("data").get("bidID").asText();
        mvc.perform(put("/api/bids/" + bidID).header("Authorization", token(outsider)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":120}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/bids/" + bidID).header("Authorization", token(bidder)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":120,\"productID\":\"different-product\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bids").header("Authorization", token(seller)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productID", product.getProductID(), "amount", 110))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void bidsRequireApprovedAccountsAndAnOpenApprovedAuction() throws Exception {
        bidder.setStatus(User.Status.PENDING);
        users.saveAndFlush(bidder);
        rejectedBid(bidder, product, 100, 403);
        bidder.setStatus(User.Status.APPROVED);
        users.saveAndFlush(bidder);
        product.setProductStatus(Product.ProductStatus.PENDING);
        products.saveAndFlush(product);
        rejectedBid(bidder, product, 100, 400);
        product.setProductStatus(Product.ProductStatus.APPROVED);
        product.setAuctionStartDate(LocalDateTime.now().plusMinutes(1));
        products.saveAndFlush(product);
        rejectedBid(bidder, product, 100, 400);
        product.setAuctionStartDate(LocalDateTime.now().minusMinutes(2));
        product.setAuctionEndDate(LocalDateTime.now().minusMinutes(1));
        products.saveAndFlush(product);
        rejectedBid(bidder, product, 100, 400);
        rejectedBid(bidder, product, -1, 400);
    }

    @Test
    @Transactional
    void settlementUsesWinnerAndHighestAmountAndIsPrivateAndIdempotent() throws Exception {
        bid(outsider, product, 100);
        bid(bidder, product, 130);
        mvc.perform(post("/api/invoices").param("productID", product.getProductID()).header("Authorization", token(bidder)))
                .andExpect(status().isBadRequest());
        closeProduct();
        mvc.perform(post("/api/invoices").param("productID", product.getProductID()).header("Authorization", token(outsider)))
                .andExpect(status().isForbidden());
        JsonNode invoice = data(mvc.perform(post("/api/invoices").param("productID", product.getProductID())
                .header("Authorization", token(seller))).andExpect(status().isOk()).andReturn());
        String invoiceID = invoice.get("invoiceID").asText();
        assertEquals(0, new BigDecimal("130").compareTo(invoice.get("amount").decimalValue()));
        assertEquals(bidder.getUserID(), invoice.get("buyerID").asText());
        assertEquals(seller.getUserID(), invoice.get("sellerID").asText());
        assertEquals(seller.getUserID(), audit.findByEntityTypeAndEntityID("INVOICE", invoiceID).get(0).getPerformedBy());
        assertEquals(Product.ProductStatus.SOLD, products.findById(product.getProductID()).orElseThrow().getProductStatus());
        mvc.perform(post("/api/invoices").param("productID", product.getProductID()).header("Authorization", token(bidder)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.invoiceID").value(invoiceID));
        assertEquals(1, invoices.findByProductID(product.getProductID()).stream().count());
        mvc.perform(get("/api/invoices/" + invoiceID).header("Authorization", token(outsider))).andExpect(status().isForbidden());
        mvc.perform(get("/api/invoices/" + invoiceID + "/download").header("Authorization", token(outsider))).andExpect(status().isForbidden());
        byte[] pdf = mvc.perform(get("/api/invoices/" + invoiceID + "/download").header("Authorization", token(bidder)))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF)).andReturn().getResponse().getContentAsByteArray();
        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
        mvc.perform(put("/api/invoices/" + invoiceID + "/payment-status").param("paymentStatus", "PAID").header("Authorization", token(bidder)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/invoices/" + invoiceID + "/payment-status").header("Authorization", token(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"paymentStatus\":\"PAID\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
        for (String downgrade : List.of("FAILED", "PENDING")) {
            mvc.perform(put("/api/invoices/" + invoiceID + "/payment-status").header("Authorization", token(admin))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("paymentStatus", downgrade))))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(Invoice.PaymentStatus.PAID, invoices.findById(invoiceID).orElseThrow().getPaymentStatus());
        assertEquals(2, audit.findByEntityTypeAndEntityID("INVOICE", invoiceID).size(), "Creation and one payment transition are recorded; rejected downgrades are not transitions");
    }

    @Test
    void concurrentEqualBidsAcceptExactlyOne() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Integer> place = () -> {
                ready.countDown();
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return mvc.perform(post("/api/bids").header("Authorization", token(bidder)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("productID", product.getProductID(), "amount", 100))))
                        .andReturn().getResponse().getStatus();
            };
            Future<Integer> first = executor.submit(place);
            Future<Integer> second = executor.submit(place);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            List<Integer> statuses = java.util.stream.Stream.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)).sorted().toList();
            assertEquals(List.of(200, 400), statuses);
            assertEquals(1, bids.findByProductIDOrderByAmountDescTimestampAsc(product.getProductID()).size());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentSettlementCreatesOneInvoice() throws Exception {
        bid(bidder, product, 100);
        closeProduct();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<String> settle = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return data(mvc.perform(post("/api/invoices").param("productID", product.getProductID())
                        .header("Authorization", token(bidder))).andExpect(status().isOk()).andReturn())
                        .get("invoiceID").asText();
            };
            Future<String> first = executor.submit(settle);
            Future<String> second = executor.submit(settle);
            start.countDown();
            assertEquals(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertEquals(1, invoices.findByProductID(product.getProductID()).stream().count());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private JsonNode bid(User account, Product target, int amount) throws Exception {
        return json.readTree(mvc.perform(post("/api/bids").header("Authorization", token(account)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productID", target.getProductID(), "amount", amount, "bidderID", "forged"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private void rejectedBid(User account, Product target, int amount, int expectedStatus) throws Exception {
        mvc.perform(post("/api/bids").header("Authorization", token(account)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productID", target.getProductID(), "amount", amount))))
                .andExpect(status().is(expectedStatus));
    }
    private JsonNode data(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsString()).get("data"); }
    private void closeProduct() {
        product.setAuctionEndDate(LocalDateTime.now().minusSeconds(1));
        products.saveAndFlush(product);
    }
    private String token(User account) { return "Bearer " + jwt.generateToken(account.getEmail(), account.getRole().name()); }
    private String id() { return UUID.randomUUID().toString(); }
    private User user(User.Role role) {
        String id = id();
        return users.saveAndFlush(new User(id, "Test", role.name(), "user-" + id.substring(0, 8), id + "@example.test", "1234567890", "test-hash", role, User.Status.APPROVED));
    }
    private Product product(LocalDateTime start, LocalDateTime end) {
        Product target = new Product();
        target.setProductID(id());
        target.setTitle("Auction domain fixture");
        target.setDescription("Integration test item");
        target.setSellerID(seller.getUserID());
        target.setCategoryID(category.getCategoryID());
        target.setProductStatus(Product.ProductStatus.APPROVED);
        target.setAuctionStartDate(start);
        target.setAuctionEndDate(end);
        target.setStartingPrice(new BigDecimal("100.00"));
        target.setIncrementGap(new BigDecimal("10.00"));
        target.setProductCondition(Product.ProductCondition.USED);
        target.setPromotionStatus(Product.PromotionStatus.NONE);
        products.saveAndFlush(target);
        Auction auction = new Auction(id(), target.getTitle(), target.getProductID(), start, end, target.getStartingPrice(), Auction.Status.ONGOING, Auction.AuctionType.STANDARD, admin.getUserID());
        auctions.saveAndFlush(auction);
        target.setAuctionID(auction.getAuctionID());
        return products.saveAndFlush(target);
    }
}
