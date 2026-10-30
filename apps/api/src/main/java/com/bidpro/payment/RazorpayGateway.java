package com.bidpro.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/** Server-side REST adapter. Only hosted Checkout receives the public key. */
@Component
public class RazorpayGateway {
    private final String keyID;
    private final String keySecret;
    private final String webhookSecret;
    private final boolean enabled;
    private final boolean allowLive;
    private final URI api;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public RazorpayGateway(ObjectMapper json, Environment environment,
        @Value("${payments.enabled:${PAYMENTS_ENABLED:false}}") boolean enabled,
        @Value("${payments.razorpay.key-id:${RAZORPAY_KEY_ID:}}") String keyID,
        @Value("${payments.razorpay.key-secret:${RAZORPAY_KEY_SECRET:}}") String keySecret,
        @Value("${payments.razorpay.webhook-secret:${RAZORPAY_WEBHOOK_SECRET:}}") String webhookSecret,
        @Value("${payments.razorpay.allow-live:${RAZORPAY_ALLOW_LIVE:false}}") boolean allowLive,
        @Value("${payments.razorpay.api-url:https://api.razorpay.com/v1}") String api) {
        this.json=json;this.enabled=enabled;this.keyID=keyID.trim();this.keySecret=keySecret.trim();
        this.webhookSecret=webhookSecret;this.allowLive=allowLive;
        this.api=URI.create(api.replaceAll("/+$", "") + "/");
        boolean localTest=environment.acceptsProfiles(Profiles.of("test")) &&
            "http".equals(this.api.getScheme()) && ("localhost".equals(this.api.getHost()) || "127.0.0.1".equals(this.api.getHost()));
        if (!"https".equals(this.api.getScheme()) && !localTest) throw new IllegalArgumentException("The payment API must use HTTPS");
        if (this.api.getUserInfo()!=null || this.api.getQuery()!=null || this.api.getFragment()!=null) throw new IllegalArgumentException("Invalid payment API URL");
    }
    public boolean requested() { return enabled; }
    public boolean enabled() {
        return enabled && !keySecret.isBlank() && !webhookSecret.isBlank() &&
            (keyID.startsWith("rzp_test_") || allowLive && keyID.startsWith("rzp_live_"));
    }
    public String mode() { return keyID.startsWith("rzp_live_") ? "live" : "test"; }
    public String keyID() { return keyID; }
    String keySecret() { return keySecret; }
    String webhookSecret() { return webhookSecret; }
    public void requireEnabled() {
        if (!enabled()) throw new PaymentUnavailableException("Online payments are not configured. Contact the marketplace administrator.");
    }
    public JsonNode createOrder(String invoiceID,long amountMinor) {
        requireEnabled();
        return call("POST", "orders", Map.of("amount", amountMinor, "currency", "INR", "receipt", invoiceID,
            "notes", Map.of("invoice_id",invoiceID)));
    }
    public JsonNode payment(String paymentID) {
        requireEnabled();
        if (paymentID==null || !paymentID.matches("pay_[A-Za-z0-9]{1,50}")) throw new IllegalArgumentException("Invalid payment identifier");
        return call("GET", "payments/"+paymentID, null);
    }
    private JsonNode call(String method,String path,Map<String,Object> body) {
        try {
            var builder=HttpRequest.newBuilder(api.resolve(path)).timeout(Duration.ofSeconds(12))
                .header("Authorization","Basic "+Base64.getEncoder().encodeToString((keyID+":"+keySecret).getBytes(StandardCharsets.UTF_8)))
                .header("Accept","application/json");
            if (body!=null) builder.header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            else builder.GET();
            var response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode()==404) throw new IllegalArgumentException("The payment could not be found at the provider");
            if (response.statusCode()<200 || response.statusCode()>=300) throw new PaymentGatewayException("The payment provider could not complete this request. Please try again.");
            JsonNode data=json.readTree(response.body());
            if (data==null || !data.isObject() || data.has("error")) throw new PaymentGatewayException("The payment provider returned an invalid response");
            return data;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();throw new PaymentGatewayException("The payment request was interrupted. Please try again.");
        } catch (java.io.IOException error) {
            throw new PaymentGatewayException("Cannot reach the payment provider. Please try again.");
        }
    }
}
