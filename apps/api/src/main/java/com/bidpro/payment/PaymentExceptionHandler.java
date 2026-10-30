package com.bidpro.payment;
import com.bidpro.dto.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice @Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentExceptionHandler {
    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiResponse<Void>> gateway(PaymentGatewayException error) {
        return ResponseEntity.status(502).body(ApiResponse.error(error.getMessage(), "PAYMENT_PROVIDER_ERROR", 502));
    }
    @ExceptionHandler(PaymentUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> disabled(PaymentUnavailableException error) {
        return ResponseEntity.status(503).body(ApiResponse.error(error.getMessage(), "PAYMENTS_UNAVAILABLE", 503));
    }
}
