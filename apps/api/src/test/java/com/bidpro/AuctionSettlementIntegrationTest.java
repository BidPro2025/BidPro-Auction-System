package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.service.AuctionSettlementService;
import com.bidpro.service.InvoiceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuctionSettlementIntegrationTest {
    @Autowired UserDao users;
    @Autowired CategoryDao categories;
    @Autowired ProductDao products;
    @Autowired AuctionDao auctions;
    @Autowired BidDao bids;
    @Autowired InvoiceDao invoices;
    @Autowired InvoiceService invoiceService;
    @Autowired AuctionSettlementService settlements;
    @Autowired LogDao audit;

    @Test
    void schedulerSelectsOnlyEndedPublishedAuctionsWithWinningBids() {
        Product ended = fixture(-1, Product.ProductStatus.APPROVED, true);
        Product future = fixture(60, Product.ProductStatus.APPROVED, true);
        Product empty = fixture(-1, Product.ProductStatus.APPROVED, false);
        Product rejected = fixture(-1, Product.ProductStatus.REJECTED, true);

        assertTrue(settlements.ready().contains(ended.getProductID()));
        assertFalse(settlements.ready().contains(future.getProductID()));
        assertFalse(settlements.ready().contains(empty.getProductID()));
        assertFalse(settlements.ready().contains(rejected.getProductID()));
        settlements.settle(ended.getProductID());
        Invoice invoice = invoices.findByProductID(ended.getProductID()).orElseThrow();
        assertEquals(new BigDecimal("125.50"), invoice.getAmount());
        assertEquals(Invoice.PaymentStatus.PENDING, invoice.getPaymentStatus());
        assertEquals("SYSTEM_SETTLEMENT", audit.findByEntityTypeAndEntityID("INVOICE", invoice.getInvoiceID()).get(0).getPerformedBy());
        assertEquals(Product.ProductStatus.SOLD, products.findById(ended.getProductID()).orElseThrow().getProductStatus());
        assertFalse(settlements.ready().contains(ended.getProductID()));
        assertTrue(invoiceService.settleExpiredAuction(future.getProductID()).isEmpty());
        assertTrue(invoiceService.settleExpiredAuction(empty.getProductID()).isEmpty());
        assertTrue(invoiceService.settleExpiredAuction(rejected.getProductID()).isEmpty());
        assertTrue(invoices.findByProductID(future.getProductID()).isEmpty());
        assertTrue(invoices.findByProductID(empty.getProductID()).isEmpty());
    }

    @Test
    void concurrentSystemSettlementCreatesOnlyOneInvoice() throws Exception {
        Product product = fixture(-1, Product.ProductStatus.APPROVED, true);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<String> settle = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return invoiceService.settleExpiredAuction(product.getProductID()).orElseThrow().getInvoiceID();
            };
            Future<String> first = pool.submit(settle);
            Future<String> second = pool.submit(settle);
            start.countDown();
            assertEquals(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertEquals(1, invoices.findByProductID(product.getProductID()).stream().count());
            assertEquals(1, audit.findByEntityTypeAndEntityID("INVOICE", first.get()).size(), "Concurrent retries append exactly one settlement audit event");
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private Product fixture(long endOffset, Product.ProductStatus status, boolean withBid) {
        User seller = user(User.Role.SELLER);
        User bidder = user(User.Role.BIDDER);
        User admin = user(User.Role.ADMIN);
        Category category = new Category();
        category.setCategoryID(id());
        category.setName("Settlement " + id());
        categories.saveAndFlush(category);
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusSeconds(endOffset);
        Product product = new Product();
        product.setProductID(id());
        product.setTitle("Automatic settlement fixture");
        product.setDescription("A deterministic closed or upcoming fixture");
        product.setSellerID(seller.getUserID());
        product.setCategoryID(category.getCategoryID());
        product.setProductStatus(status);
        product.setAuctionStartDate(start);
        product.setAuctionEndDate(end);
        product.setStartingPrice(new BigDecimal("100.00"));
        product.setIncrementGap(new BigDecimal("5.00"));
        product.setProductCondition(Product.ProductCondition.USED);
        product.setPromotionStatus(Product.PromotionStatus.NONE);
        products.saveAndFlush(product);
        Auction auction = new Auction(id(), product.getTitle(), product.getProductID(), start, end,
                product.getStartingPrice(), Auction.Status.ONGOING, Auction.AuctionType.STANDARD, admin.getUserID());
        auctions.saveAndFlush(auction);
        product.setAuctionID(auction.getAuctionID());
        products.saveAndFlush(product);
        if (withBid) {
            Bid bid = new Bid();
            bid.setBidID(id());
            bid.setProductID(product.getProductID());
            bid.setBidderID(bidder.getUserID());
            bid.setAmount(new BigDecimal("125.50"));
            bid.setTimestamp(start.plusMinutes(1));
            bids.saveAndFlush(bid);
        }
        return product;
    }

    private User user(User.Role role) {
        String id = id();
        return users.saveAndFlush(new User(id, "Settlement", "Test", "settle-" + id.substring(0, 8),
                id + "@example.test", "1234567890", "test-hash", role, User.Status.APPROVED));
    }

    private String id() { return UUID.randomUUID().toString(); }
}
