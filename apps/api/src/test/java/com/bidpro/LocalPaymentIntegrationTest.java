package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.payment.PaymentRecordRepository;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"payments.enabled=false","payments.local-test=true","app.seed.enabled=false"})
@AutoConfigureMockMvc @ActiveProfiles({"local","test"}) @Transactional
class LocalPaymentIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired JwtUtil jwt;
    @Autowired UserDao users; @Autowired CategoryDao categories; @Autowired ProductDao products;
    @Autowired AuctionDao auctions; @Autowired InvoiceDao invoices; @Autowired NotificationDao notifications;
    @Autowired PaymentRecordRepository records;
    User buyer,seller,outsider,admin; Invoice invoice;
    @BeforeEach void fixture() {
        buyer=user(User.Role.BIDDER);seller=user(User.Role.SELLER);outsider=user(User.Role.BIDDER);admin=user(User.Role.ADMIN);
        Category category=new Category();category.setCategoryID(id());category.setName("Local payment "+id().substring(0,8));categories.saveAndFlush(category);
        var start=LocalDateTime.now().minusDays(1);var end=LocalDateTime.now().minusHours(1);
        Product product=new Product(id(),"Local test item","Local checkout integration",seller.getUserID(),category.getCategoryID(),null,
            Product.ProductStatus.SOLD,start,end,new BigDecimal("100.00"),new BigDecimal("10.00"),Product.ProductCondition.USED,Product.PromotionStatus.NONE);products.saveAndFlush(product);
        Auction auction=new Auction(id(),product.getTitle(),product.getProductID(),start,end,product.getStartingPrice(),Auction.Status.PAST,Auction.AuctionType.STANDARD,admin.getUserID());auctions.saveAndFlush(auction);
        product.setAuctionID(auction.getAuctionID());products.saveAndFlush(product);
        invoice=new Invoice();invoice.setInvoiceID(id());invoice.setProductID(product.getProductID());invoice.setAuctionID(auction.getAuctionID());invoice.setSellerID(seller.getUserID());
        invoice.setBuyerID(buyer.getUserID());invoice.setAmount(new BigDecimal("110.00"));invoice.setInvoiceDate(LocalDateTime.now());invoices.saveAndFlush(invoice);
    }
    @Test void localConfigurationAndOrdersAreExplicitAndBuyerOnly() throws Exception {
        mvc.perform(get("/api/payments/config").header("Authorization",token(buyer))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.provider").value("LOCAL_TEST")).andExpect(jsonPath("$.data.mode").value("local-test")).andExpect(jsonPath("$.data.enabled").value(true));
        String first=order();assertEquals(first,order());
        for(User other:List.of(seller,outsider,admin)) mvc.perform(post("/api/payments/local-test").header("Authorization",token(other)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID(),"outcome","SUCCESS")))).andExpect(status().isForbidden());
        assertEquals(Invoice.PaymentStatus.PENDING,invoices.findById(invoice.getInvoiceID()).orElseThrow().getPaymentStatus());
    }
    @Test void failedCheckoutRetriesAndSuccessfulCheckoutIsIdempotentAndClearlyTestOnly() throws Exception {
        order();outcome("FAILURE").andExpect(status().isOk()).andExpect(jsonPath("$.data.paymentStatus").value("FAILED"));
        String existing=order();assertTrue(existing.startsWith("local_order_"));long before=notifications.count();
        outcome("SUCCESS").andExpect(status().isOk()).andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
        outcome("SUCCESS").andExpect(status().isOk());outcome("FAILURE").andExpect(status().isOk()).andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
        assertEquals(before+2,notifications.count());
        Invoice paid=invoices.findById(invoice.getInvoiceID()).orElseThrow();assertEquals(Invoice.PaymentSource.LOCAL_TEST,paid.getPaymentSource());
        mvc.perform(get("/api/invoices/mine").header("Authorization",token(buyer))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].paymentSource").value("LOCAL_TEST")).andExpect(jsonPath("$.data[0].paymentMode").value("local-test"));
        byte[] pdf=mvc.perform(get("/api/invoices/"+invoice.getInvoiceID()+"/download").header("Authorization",token(buyer))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try(var document=Loader.loadPDF(pdf)) {String text=new PDFTextStripper().getText(document);assertTrue(text.contains("LOCAL_TEST"));assertTrue(text.contains("no real money transferred"));}
    }
    @Test void localOutcomeRequiresAnOrderAndValidOutcome() throws Exception {
        outcome("SUCCESS").andExpect(status().isNotFound());order();outcome("FAKE").andExpect(status().isBadRequest());
        assertEquals(Invoice.PaymentStatus.PENDING,invoices.findById(invoice.getInvoiceID()).orElseThrow().getPaymentStatus());
    }
    private String order() throws Exception {
        var response=mvc.perform(post("/api/payments/orders").header("Authorization",token(buyer)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID())))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mode").value("local-test")).andExpect(jsonPath("$.data.amount").value(11000)).andReturn();
        return json.readTree(response.getResponse().getContentAsString()).at("/data/orderID").asText();
    }
    private org.springframework.test.web.servlet.ResultActions outcome(String outcome) throws Exception {
        return mvc.perform(post("/api/payments/local-test").header("Authorization",token(buyer)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID(),"outcome",outcome))));
    }
    private String id(){return UUID.randomUUID().toString();}
    private User user(User.Role role){String id=id();return users.saveAndFlush(new User(id,"Local",role.name(),id,id+"@example.test","","unused",role,User.Status.APPROVED));}
    private String token(User account){return "Bearer "+jwt.generateToken(account.getEmail(),account.getRole().name());}
}
