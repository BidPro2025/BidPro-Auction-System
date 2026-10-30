package com.bidpro.payment;

import com.bidpro.dao.InvoiceDao;
import com.bidpro.entities.Invoice;
import com.bidpro.service.CurrentUserService;
import com.bidpro.service.NotificationService;
import com.bidpro.service.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly=true)
public class PaymentService {
    private final InvoiceDao invoices;
    private final PaymentRecordRepository orders;
    private final PaymentWebhookRepository events;
    private final RazorpayGateway gateway;
    private final CurrentUserService current;
    private final NotificationService notifications;
    private final ObjectMapper json;
    private final boolean localTestAllowed;
    private final LogService audit;
    public PaymentService(InvoiceDao invoices,PaymentRecordRepository orders,PaymentWebhookRepository events,
            RazorpayGateway gateway,CurrentUserService current,NotificationService notifications,ObjectMapper json,
            Environment environment,@Value("${payments.local-test:${PAYMENTS_LOCAL_TEST:false}}") boolean localTest,LogService audit) {
        this.invoices=invoices;this.orders=orders;this.events=events;this.gateway=gateway;
        this.current=current;this.notifications=notifications;this.json=json;
        this.localTestAllowed=localTest && environment.acceptsProfiles(Profiles.of("local"));
        this.audit=audit;
    }
    private boolean useLocalTest() { return localTestAllowed && !gateway.requested(); }
    public Configuration configuration() {
        if(useLocalTest()) return new Configuration(true,"LOCAL_TEST","local-test","Local test checkout: no money is transferred.");
        return new Configuration(gateway.enabled(),"RAZORPAY",gateway.mode(),gateway.enabled()
            ? gateway.mode().equals("test") ? "Test mode: no real money is transferred." : "Secure online payments are available."
            : "Online payments are not configured. Contact the marketplace administrator.");
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public CheckoutOrder createOrder(String invoiceID) {
        current.requireApproved();
        Invoice invoice=lockedInvoice(invoiceID);
        requireBuyer(invoice);
        if(!useLocalTest()) gateway.requireEnabled();
        if(invoice.getPaymentStatus()==Invoice.PaymentStatus.PAID) throw new IllegalArgumentException("This invoice is already paid");
        long amount=invoice.getAmount().movePointRight(2).longValueExact();
        if(amount<=0) throw new IllegalArgumentException("The invoice amount must be positive");
        PaymentRecord existing=orders.findByInvoiceID(invoiceID).orElse(null);
        if(existing!=null) {
            if(!existing.getMode().equals(useLocalTest()?"local-test":gateway.mode())) throw new IllegalArgumentException("This invoice has an order from a different payment mode. Contact the administrator.");
            return checkout(existing);
        }
        if(useLocalTest()) {
            PaymentRecord order=new PaymentRecord();order.setId(UUID.randomUUID().toString());order.setInvoiceID(invoiceID);
            order.setOrderID("local_order_"+UUID.randomUUID().toString().replace("-",""));order.setAmountMinor(amount);order.setCurrency("INR");
            order.setMode("local-test");order.setStatus(PaymentRecord.Status.CREATED);
            orders.saveAndFlush(order);
            audit.logAction("PAYMENT",invoiceID,"ORDER_CREATED",current.id(),"mode=local-test;order="+order.getOrderID()+";amountMinor="+amount+";currency=INR");
            return checkout(order);
        }
        JsonNode remote=gateway.createOrder(invoiceID,amount);
        String orderID=remote.path("id").asText();
        if(!orderID.matches("order_[A-Za-z0-9]{1,50}") || remote.path("amount").asLong(-1)!=amount ||
            !"INR".equals(remote.path("currency").asText())) throw new PaymentGatewayException("The payment provider returned inconsistent order details");
        PaymentRecord order=new PaymentRecord();order.setId(UUID.randomUUID().toString());order.setInvoiceID(invoiceID);
        order.setOrderID(orderID);order.setAmountMinor(amount);order.setCurrency("INR");
        order.setMode(gateway.mode());order.setStatus(PaymentRecord.Status.CREATED);
        orders.saveAndFlush(order);
        audit.logAction("PAYMENT",invoiceID,"ORDER_CREATED",current.id(),"mode="+order.getMode()+";order="+orderID+";amountMinor="+amount+";currency=INR");
        return checkout(order);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public PaymentResult localTest(PaymentController.LocalTest input) {
        current.requireApproved();
        if(!useLocalTest()) throw new PaymentUnavailableException("Local test checkout is available only in the local development profile");
        Invoice invoice=lockedInvoice(input.invoiceID());requireBuyer(invoice);
        PaymentRecord order=orders.findByInvoiceID(input.invoiceID()).orElseThrow(()->new EntityNotFoundException("Create a checkout order first"));
        if(!"local-test".equals(order.getMode())) throw new IllegalArgumentException("This is not a local test order");
        if(order.getStatus()==PaymentRecord.Status.CAPTURED) return result(order,invoice);
        if(invoice.getPaymentStatus()==Invoice.PaymentStatus.PAID) throw new IllegalArgumentException("This invoice is already paid");
        if(order.getAmountMinor()!=invoice.getAmount().movePointRight(2).longValueExact()) throw new IllegalArgumentException("Invoice amount changed");
        order.setUpdatedAt(LocalDateTime.now());
        if(order.getPaymentID()==null) order.setPaymentID("local_payment_"+UUID.randomUUID().toString().replace("-",""));
        invoice.setPaymentSource(Invoice.PaymentSource.LOCAL_TEST);invoice.setUpdatedAt(LocalDateTime.now());
        if("SUCCESS".equals(input.outcome())) {
            order.setStatus(PaymentRecord.Status.CAPTURED);invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);invoice.setPaymentReference(order.getPaymentID());
            notifications.notifyUser(invoice.getBuyerID(),"LOCAL_TEST_PAYMENT","Local test payment completed for invoice "+invoice.getInvoiceID()+". No money was transferred.","/bidder/invoices");
            notifications.notifyUser(invoice.getSellerID(),"LOCAL_TEST_PAYMENT","Local test payment completed for invoice "+invoice.getInvoiceID()+". No money was transferred.","/seller/invoices");
        } else if("FAILURE".equals(input.outcome())) {order.setStatus(PaymentRecord.Status.FAILED);invoice.setPaymentStatus(Invoice.PaymentStatus.FAILED);}
        else throw new IllegalArgumentException("Choose SUCCESS or FAILURE");
        invoices.save(invoice);orders.saveAndFlush(order);
        audit.logAction("PAYMENT",invoice.getInvoiceID(),"LOCAL_TEST_"+order.getStatus().name(),current.id(),"mode=local-test;order="+order.getOrderID());
        return result(order,invoice);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public PaymentResult verify(PaymentController.Verification input) {
        current.requireApproved();
        Invoice invoice=lockedInvoice(input.invoiceID());requireBuyer(invoice);gateway.requireEnabled();
        PaymentRecord order=orders.findByInvoiceID(input.invoiceID()).orElseThrow(()->new EntityNotFoundException("Payment order not found"));
        if(!order.getOrderID().equals(input.razorpay_order_id()) ||
            !validSignature((order.getOrderID()+"|"+input.razorpay_payment_id()).getBytes(StandardCharsets.UTF_8),gateway.keySecret(),input.razorpay_signature()))
            throw new IllegalArgumentException("Payment verification failed. The signature is invalid.");
        if(order.getStatus()==PaymentRecord.Status.CAPTURED && input.razorpay_payment_id().equals(order.getPaymentID())) return result(order,invoice);
        applyPayment(order,invoice,gateway.payment(input.razorpay_payment_id()),current.id());
        return result(order,invoice);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public void webhook(byte[] body,String signature,String eventID) {
        gateway.requireEnabled();
        if(body==null || body.length==0 || body.length>262144 || eventID==null || !eventID.matches("[A-Za-z0-9_-]{1,128}") ||
            !validSignature(body,gateway.webhookSecret(),signature)) throw new IllegalArgumentException("Invalid payment webhook signature or event identifier");
        String hash=sha256(body);
        if(alreadyProcessed(eventID,hash)) return;
        JsonNode payload;
        try {payload=json.readTree(body);}catch(java.io.IOException error){throw new IllegalArgumentException("Invalid webhook body");}
        String type=payload.path("event").asText();
        if(type.length()>64) throw new IllegalArgumentException("Invalid webhook event");
        JsonNode payment=payload.path("payload").path("payment").path("entity");
        var found=orders.findByOrderID(payment.path("order_id").asText());
        if(found.isPresent() && Set.of("payment.captured","payment.authorized","payment.failed","order.paid").contains(type)) {
            Invoice invoice=lockedInvoice(found.get().getInvoiceID());
            // Recheck after acquiring the same invoice lock used by callbacks.
            if(alreadyProcessed(eventID,hash)) return;
            PaymentRecord order=orders.findByInvoiceID(invoice.getInvoiceID()).orElseThrow();
            if(order.getStatus()!=PaymentRecord.Status.CAPTURED) applyPayment(order,invoice,gateway.payment(payment.path("id").asText()),"RAZORPAY_WEBHOOK");
        }
        PaymentWebhookEvent event=new PaymentWebhookEvent();event.setId(eventID);event.setBodyHash(hash);event.setEventType(type);
        events.saveAndFlush(event);
    }
    private boolean alreadyProcessed(String eventID,String hash) {
        var existing=events.findById(eventID);
        if(existing.isEmpty()) return false;
        if(!existing.get().getBodyHash().equals(hash)) throw new IllegalArgumentException("Webhook event identifier was already used for different data");
        return true;
    }
    private void applyPayment(PaymentRecord order,Invoice invoice,JsonNode payment,String actor) {
        String paymentID=payment.path("id").asText();
        if(!paymentID.matches("pay_[A-Za-z0-9]{1,50}") || !order.getOrderID().equals(payment.path("order_id").asText()) ||
            payment.path("amount").asLong(-1)!=order.getAmountMinor() || !order.getCurrency().equals(payment.path("currency").asText()))
            throw new IllegalArgumentException("The payment does not match this invoice's order, amount or currency");
        if(order.getStatus()==PaymentRecord.Status.CAPTURED) return;
        String state=payment.path("status").asText();
        boolean captured="captured".equals(state) && payment.path("captured").asBoolean(false);
        if("captured".equals(state) && !captured) throw new IllegalArgumentException("The provider has not captured this payment");
        order.setPaymentID(paymentID);order.setUpdatedAt(LocalDateTime.now());
        if(captured) {
            order.setStatus(PaymentRecord.Status.CAPTURED);
            invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);invoice.setPaymentSource(Invoice.PaymentSource.RAZORPAY);
            invoice.setPaymentReference(paymentID);invoice.setUpdatedAt(LocalDateTime.now());invoices.save(invoice);
            notifications.notifyUser(invoice.getBuyerID(),"PAYMENT_CAPTURED","Payment received for invoice "+invoice.getInvoiceID(),"/bidder/invoices");
            notifications.notifyUser(invoice.getSellerID(),"PAYMENT_CAPTURED","Your auction invoice has been paid: "+invoice.getInvoiceID(),"/seller/invoices");
        } else if("authorized".equals(state)) {
            order.setStatus(PaymentRecord.Status.AUTHORIZED);
        } else if("failed".equals(state)) {
            order.setStatus(PaymentRecord.Status.FAILED);
            if(invoice.getPaymentStatus()!=Invoice.PaymentStatus.PAID) {invoice.setPaymentStatus(Invoice.PaymentStatus.FAILED);invoices.save(invoice);}
        } else throw new IllegalArgumentException("Payment is still processing. Refresh your invoices shortly.");
        orders.saveAndFlush(order);
        audit.logAction("PAYMENT",invoice.getInvoiceID(),order.getStatus().name(),actor,
                "mode="+order.getMode()+";order="+order.getOrderID()+";payment="+paymentID+";amountMinor="+order.getAmountMinor()+";currency="+order.getCurrency());
    }
    private Invoice lockedInvoice(String id) {
        return invoices.findForUpdateByInvoiceID(id).orElseThrow(()->new EntityNotFoundException("Invoice not found"));
    }
    private void requireBuyer(Invoice invoice) {
        if(!current.id().equals(invoice.getBuyerID())) throw new AccessDeniedException("Only the winning buyer can pay this invoice");
    }
    private CheckoutOrder checkout(PaymentRecord order) {
        return new CheckoutOrder(order.getInvoiceID(),order.getOrderID(),order.getAmountMinor(),order.getCurrency(),"local-test".equals(order.getMode())?"":gateway.keyID(),order.getMode(),"BidPro");
    }
    private PaymentResult result(PaymentRecord order,Invoice invoice) {
        return new PaymentResult(invoice.getInvoiceID(),order.getOrderID(),order.getPaymentID(),order.getStatus().name(),invoice.getPaymentStatus().name(),order.getMode());
    }
    static boolean validSignature(byte[] body,String secret,String signature) {
        if(secret==null || secret.isBlank() || signature==null || !signature.matches("[a-fA-F0-9]{64}")) return false;
        try {
            Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            return MessageDigest.isEqual(mac.doFinal(body),HexFormat.of().parseHex(signature));
        } catch(java.security.GeneralSecurityException | IllegalArgumentException error) {return false;}
    }
    private String sha256(byte[] body) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));}
        catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    public record Configuration(boolean enabled,String provider,String mode,String message) {}
    public record CheckoutOrder(String invoiceID,String orderID,long amount,String currency,String keyID,String mode,String name) {}
    public record PaymentResult(String invoiceID,String orderID,String paymentID,String status,String paymentStatus,String mode) {}
}
