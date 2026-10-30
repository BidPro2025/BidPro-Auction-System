package com.bidpro.payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name = "payment_webhook_event") @Getter @Setter
public class PaymentWebhookEvent {
    @Id @Column(length = 128) private String id;
    @Column(length = 64, nullable = false) private String bodyHash;
    @Column(length = 64, nullable = false) private String eventType;
    @Column(nullable = false) private LocalDateTime processedAt = LocalDateTime.now();
}
