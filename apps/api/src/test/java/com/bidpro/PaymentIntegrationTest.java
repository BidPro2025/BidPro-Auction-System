package com.bidpro;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import com.bidpro.payment.*;
import com.bidpro.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real HTTP adapter against a deterministic provider, never external payment keys. */
@SpringBootTest(properties={"payments.enabled=true","payments.razorpay.key-id=rzp_test_contract",
    "payments.razorpay.key-secret=contract-secret-not-a-real-key","payments.razorpay.webhook-secret=contract-webhook-secret"})
@AutoConfigureMockMvc @ActiveProfiles("test")
class PaymentIntegrationTest {
    private static final ObjectMapper WIRE=new ObjectMapper();
    private static final AtomicInteger sequence=new AtomicInteger();
    private static final AtomicInteger orderRequests=new AtomicInteger();
    private static final Map<String,Map<String,Object>> remotePayments=new ConcurrentHashMap<>();
    private static final HttpServer provider=startProvider();
    private static volatile JsonNode lastOrder;
    private static volatile String lastAuthorization;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("payments.razorpay.api-url",()->"http://127.0.0.1:"+provider.getAddress().getPort()+"/v1");
    }
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired JwtUtil jwt;
    @Autowired UserDao users; @Autowired CategoryDao categories; @Autowired ProductDao products;
    @Autowired AuctionDao auctions; @Autowired InvoiceDao invoices;
    @Autowired PaymentRecordRepository records; @Autowired PaymentWebhookRepository events;
    @Autowired NotificationDao notifications;
    @Autowired LogDao audit;
    User buyer,seller,outsider,admin; Invoice invoice;
    @BeforeEach void fixture() {
        orderRequests.set(0);
        buyer=user(User.Role.BIDDER);seller=user(User.Role.SELLER);outsider=user(User.Role.BIDDER);admin=user(User.Role.ADMIN);
        Category category=new Category();category.setCategoryID(id());category.setName("Payment fixture "+id().substring(0,8));categories.saveAndFlush(category);
        var start=LocalDateTime.now().minusDays(1);var end=LocalDateTime.now().minusHours(1);
        Product product=new Product(id(),"Paid auction item","Payment integration fixture",seller.getUserID(),category.getCategoryID(),null,
            Product.ProductStatus.SOLD,start,end,new BigDecimal("100.00"),new BigDecimal("10.00"),Product.ProductCondition.USED,Product.PromotionStatus.NONE);
        products.saveAndFlush(product);
        Auction auction=new Auction(id(),product.getTitle(),product.getProductID(),start,end,product.getStartingPrice(),Auction.Status.PAST,Auction.AuctionType.STANDARD,admin.getUserID());auctions.saveAndFlush(auction);
        product.setAuctionID(auction.getAuctionID());products.saveAndFlush(product);
        invoice=new Invoice();invoice.setInvoiceID(id());invoice.setProductID(product.getProductID());invoice.setAuctionID(auction.getAuctionID());
        invoice.setSellerID(seller.getUserID());invoice.setBuyerID(buyer.getUserID());invoice.setAmount(new BigDecimal("110.00"));invoice.setInvoiceDate(LocalDateTime.now());invoices.saveAndFlush(invoice);
    }
    @AfterAll static void stopProvider(){provider.stop(0);}

    @Test @Transactional void orderAmountComesFromInvoiceAndCreationIsIdempotentAndBuyerOnly() throws Exception {
        String configuration=mvc.perform(get("/api/payments/config").header("Authorization",token(buyer))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(true)).andExpect(jsonPath("$.data.mode").value("test")).andReturn().getResponse().getContentAsString();
        assertFalse(configuration.contains("contract-secret"));
        mvc.perform(post("/api/payments/orders").header("Authorization",token(seller)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID())))).andExpect(status().isForbidden());
        mvc.perform(post("/api/payments/orders").header("Authorization",token(outsider)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID())))).andExpect(status().isForbidden());
        String first=order();assertEquals(first,order());assertEquals(1,orderRequests.get());
        assertEquals(11000,lastOrder.get("amount").asLong());assertEquals("INR",lastOrder.get("currency").asText());
        assertEquals(invoice.getInvoiceID(),lastOrder.get("receipt").asText());
        assertEquals("Basic "+Base64.getEncoder().encodeToString("rzp_test_contract:contract-secret-not-a-real-key".getBytes(StandardCharsets.UTF_8)),lastAuthorization);
    }

    @Test @Transactional void forgedCallbacksCannotPayAndCapturedProviderPaymentIsRecordedOnce() throws Exception {
        String order=order();String payment="pay_"+sequence.incrementAndGet();
        remotePayments.put(payment,payment(order,payment,11000,"INR","captured",true));
        verify(order,payment,"0".repeat(64)).andExpect(status().isBadRequest());
        assertEquals(Invoice.PaymentStatus.PENDING,invoices.findById(invoice.getInvoiceID()).orElseThrow().getPaymentStatus());
        long countBefore=notifications.count();
        verify(order,payment,signature(order+"|"+payment,"contract-secret-not-a-real-key")).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CAPTURED")).andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
        verify(order,payment,signature(order+"|"+payment,"contract-secret-not-a-real-key")).andExpect(status().isOk());
        Invoice paid=invoices.findById(invoice.getInvoiceID()).orElseThrow();assertEquals(Invoice.PaymentSource.RAZORPAY,paid.getPaymentSource());assertEquals(payment,paid.getPaymentReference());
        assertEquals(countBefore+2,notifications.count());
        var paymentAudit=audit.findByEntityTypeAndEntityID("PAYMENT",invoice.getInvoiceID());
        assertEquals(1,paymentAudit.stream().filter(event->"CAPTURED".equals(event.getOperation())).count(),"Callback retries must not duplicate captured-payment audit events");
        assertTrue(paymentAudit.stream().noneMatch(event->event.getDetails().contains("contract-secret")),"Audit events do not contain provider secrets");
        mvc.perform(put("/api/invoices/"+invoice.getInvoiceID()+"/payment-status").header("Authorization",token(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"paymentStatus\":\"FAILED\"}")).andExpect(status().isBadRequest());
    }

    @Test @Transactional void authorizationIsNotPaymentAndSignedWebhookCompletesItWithoutDowngrades() throws Exception {
        String order=order();String payment="pay_"+sequence.incrementAndGet();
        remotePayments.put(payment,payment(order,payment,11000,"INR","authorized",false));
        verify(order,payment,signature(order+"|"+payment,"contract-secret-not-a-real-key")).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("AUTHORIZED")).andExpect(jsonPath("$.data.paymentStatus").value("PENDING"));
        remotePayments.put(payment,payment(order,payment,11000,"INR","captured",true));
        String eventID="event_"+id();String body=webhookBody("payment.captured",order,payment);
        webhook(body,eventID,"invalid").andExpect(status().isBadRequest());
        webhook(body,eventID,signature(body,"contract-webhook-secret")).andExpect(status().isOk());
        webhook(body,eventID,signature(body,"contract-webhook-secret")).andExpect(status().isOk());
        assertTrue(events.findById(eventID).isPresent());
        String failed=webhookBody("payment.failed",order,payment);remotePayments.put(payment,payment(order,payment,11000,"INR","failed",false));
        webhook(failed,"event_"+id(),signature(failed,"contract-webhook-secret")).andExpect(status().isOk());
        assertEquals(Invoice.PaymentStatus.PAID,invoices.findById(invoice.getInvoiceID()).orElseThrow().getPaymentStatus());
        webhook(failed,eventID,signature(failed,"contract-webhook-secret")).andExpect(status().isBadRequest());
    }

    @Test @Transactional void amountCurrencyAndOrderMustMatchVerifiedProviderState() throws Exception {
        String order=order();String payment="pay_"+sequence.incrementAndGet();String signed=signature(order+"|"+payment,"contract-secret-not-a-real-key");
        remotePayments.put(payment,payment(order,payment,1,"INR","captured",true));verify(order,payment,signed).andExpect(status().isBadRequest());
        remotePayments.put(payment,payment(order,payment,11000,"USD","captured",true));verify(order,payment,signed).andExpect(status().isBadRequest());
        remotePayments.put(payment,payment("order_someoneelse",payment,11000,"INR","captured",true));verify(order,payment,signed).andExpect(status().isBadRequest());
        remotePayments.put(payment,payment(order,payment,11000,"INR","captured",false));verify(order,payment,signed).andExpect(status().isBadRequest());
        assertEquals(Invoice.PaymentStatus.PENDING,invoices.findById(invoice.getInvoiceID()).orElseThrow().getPaymentStatus());
    }

    @Test @Transactional void failureCanRetrySameOrderAndManualRecordsRemainExplicit() throws Exception {
        String order=order();String failed="pay_"+sequence.incrementAndGet();
        remotePayments.put(failed,payment(order,failed,11000,"INR","failed",false));verify(order,failed,signature(order+"|"+failed,"contract-secret-not-a-real-key")).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("FAILED"));
        assertEquals(order,order());assertEquals(1,orderRequests.get());
        String paid="pay_"+sequence.incrementAndGet();remotePayments.put(paid,payment(order,paid,11000,"INR","captured",true));
        verify(order,paid,signature(order+"|"+paid,"contract-secret-not-a-real-key")).andExpect(status().isOk()).andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
        mvc.perform(get("/api/invoices/mine").header("Authorization",token(buyer))).andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].paymentSource").value("RAZORPAY")).andExpect(jsonPath("$.data[0].hasOnlinePaymentOrder").value(true));
    }

    @Test void concurrentCheckoutRequestsReuseOneProviderOrder() throws Exception {
        ExecutorService executor=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        Callable<String> create=()->{ready.countDown();start.await(10,TimeUnit.SECONDS);return order();};
        try {Future<String> first=executor.submit(create),second=executor.submit(create);assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            assertEquals(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS));assertEquals(1,orderRequests.get());
        }finally{start.countDown();executor.shutdownNow();}
    }

    private String order() throws Exception {
        var response=mvc.perform(post("/api/payments/orders").header("Authorization",token(buyer)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID(),"amount",1,"currency","USD"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.amount").value(11000)).andReturn();
        return json.readTree(response.getResponse().getContentAsString()).at("/data/orderID").asText();
    }
    private org.springframework.test.web.servlet.ResultActions verify(String order,String payment,String signature) throws Exception {
        return mvc.perform(post("/api/payments/verify").header("Authorization",token(buyer)).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsBytes(Map.of("invoiceID",invoice.getInvoiceID(),"razorpay_order_id",order,"razorpay_payment_id",payment,"razorpay_signature",signature))));
    }
    private org.springframework.test.web.servlet.ResultActions webhook(String body,String eventID,String signature) throws Exception {
        return mvc.perform(post("/api/payments/webhooks/razorpay").header("X-Razorpay-Event-Id",eventID).header("X-Razorpay-Signature",signature)
            .contentType(MediaType.APPLICATION_JSON).content(body));
    }
    private String webhookBody(String type,String order,String payment) throws Exception {
        return json.writeValueAsString(Map.of("event",type,"payload",Map.of("payment",Map.of("entity",Map.of("id",payment,"order_id",order)))));
    }
    private static Map<String,Object> payment(String order,String payment,long amount,String currency,String state,boolean captured) {
        return Map.of("id",payment,"order_id",order,"amount",amount,"currency",currency,"status",state,"captured",captured);
    }
    private static String signature(String message,String secret) throws Exception {
        Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }
    private String id(){return UUID.randomUUID().toString();}
    private User user(User.Role role){String id=id();return users.saveAndFlush(new User(id,"Payment",role.name(),id,id+"@example.test","","unused",role,User.Status.APPROVED));}
    private String token(User account){return "Bearer "+jwt.generateToken(account.getEmail(),account.getRole().name());}
    private static HttpServer startProvider() {
        try {
            HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/v1/orders",exchange->{
                orderRequests.incrementAndGet();lastAuthorization=exchange.getRequestHeaders().getFirst("Authorization");
                JsonNode input=WIRE.readTree(exchange.getRequestBody());lastOrder=input;
                byte[] response=WIRE.writeValueAsBytes(Map.of("id","order_"+sequence.incrementAndGet(),"amount",input.path("amount").asLong(),"currency",input.path("currency").asText(),"status","created"));
                exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);exchange.close();
            });
            server.createContext("/v1/payments/",exchange->{
                String id=exchange.getRequestURI().getPath().substring("/v1/payments/".length());Map<String,Object> payment=remotePayments.get(id);
                byte[] response=WIRE.writeValueAsBytes(payment==null?Map.of("error","not found"):payment);
                exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(payment==null?404:200,response.length);exchange.getResponseBody().write(response);exchange.close();
            });
            server.start();return server;
        }catch(java.io.IOException error){throw new ExceptionInInitializerError(error);}
    }
}
