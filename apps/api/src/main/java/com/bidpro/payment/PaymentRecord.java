package com.bidpro.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** One provider order per invoice; no card or bank details are stored. */
@Entity
@Table(name = "payment_order", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_invoice", columnNames = "invoice_id"),
    @UniqueConstraint(name = "uk_payment_provider_order", columnNames = "provider_order_id"),
    @UniqueConstraint(name = "uk_payment_provider_payment", columnNames = "provider_payment_id")
})
@Getter @Setter
public class PaymentRecord {
    @Id @Column(length = 50) private String id;
    @Column(name = "invoice_id", length = 50, nullable = false) private String invoiceID;
    @Column(name = "provider_order_id", length = 64, nullable = false) private String orderID;
    @Column(name = "provider_payment_id", length = 64) private String paymentID;
    @Column(nullable = false) private long amountMinor;
    @Column(length = 3, nullable = false) private String currency = "INR";
    @Enumerated(EnumType.STRING) @Column(length = 20, nullable = false) private Status status;
    @Column(length = 20, nullable = false) private String mode;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    public enum Status { CREATED, AUTHORIZED, CAPTURED, FAILED }
}
