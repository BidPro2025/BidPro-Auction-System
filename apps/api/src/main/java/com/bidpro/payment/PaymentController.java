package com.bidpro.payment;
import com.bidpro.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) {this.service=service;}
    @GetMapping("/config") public ApiResponse<PaymentService.Configuration> configuration() {
        return ApiResponse.success(service.configuration(),"Payment configuration");
    }
    @PostMapping("/orders") public ApiResponse<PaymentService.CheckoutOrder> order(@Valid @RequestBody OrderRequest input) {
        return ApiResponse.success(service.createOrder(input.invoiceID()),"Checkout order ready");
    }
    @PostMapping("/local-test") public ApiResponse<PaymentService.PaymentResult> localTest(@Valid @RequestBody LocalTest input) {
        return ApiResponse.success(service.localTest(input),"Local test payment processed; no money transferred");
    }
    @PostMapping("/verify") public ApiResponse<PaymentService.PaymentResult> verify(@Valid @RequestBody Verification input) {
        return ApiResponse.success(service.verify(input),"Payment status verified");
    }
    @PostMapping(value="/webhooks/razorpay",consumes="application/json")
    public ResponseEntity<ApiResponse<String>> webhook(@RequestBody byte[] body,
        @RequestHeader(value="X-Razorpay-Signature",required=false) String signature,
        @RequestHeader(value="X-Razorpay-Event-Id",required=false) String eventID) {
        service.webhook(body,signature,eventID);
        return ResponseEntity.ok(ApiResponse.success("received","Webhook processed"));
    }
    public record LocalTest(@NotBlank @Size(max=50) String invoiceID,
        @NotBlank @jakarta.validation.constraints.Pattern(regexp="SUCCESS|FAILURE") String outcome) {}
    public record OrderRequest(@NotBlank @Size(max=50) String invoiceID) {}
    public record Verification(@NotBlank @Size(max=50) String invoiceID,
        @NotBlank @Size(max=64) String razorpay_order_id,
        @NotBlank @Size(max=64) String razorpay_payment_id,
        @NotBlank @Size(max=128) String razorpay_signature) {}
}
