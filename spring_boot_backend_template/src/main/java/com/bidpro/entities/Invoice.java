package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class Invoice {

    @Id
    @Column(name = "invoiceID", length = 50)
    private String invoiceID;

    @Column(name = "auctionID", length = 50, nullable = false)
    private String auctionID;

    @Column(name = "productID", length = 50, nullable = false)
    private String productID;

    @Column(name = "buyerID", length = 50, nullable = false)
    private String buyerID;

    @Column(name = "sellerID", length = 50, nullable = false)
    private String sellerID;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "invoiceDate", nullable = false)
    private LocalDateTime invoiceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionID", insertable = false, updatable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerID", insertable = false, updatable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerID", insertable = false, updatable = false)
    private User seller;

    public enum PaymentStatus { PENDING, PAID, FAILED }

    public Invoice() {}
}