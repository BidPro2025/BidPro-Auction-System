package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid", indexes = @Index(name = "idx_bid_product_amount", columnList = "productID,amount"))
@Getter
@Setter
public class Bid {
    @Id @Column(name = "bidID", length = 50)
    private String bidID;
    @Column(name = "productID", length = 50, nullable = false)
    private String productID;
    @Column(name = "bidderID", length = 50, nullable = false)
    private String bidderID;
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "extension1", length = 255)
    private String extension1;
    @Column(name = "extension2", length = 255)
    private String extension2;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID", insertable = false, updatable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidderID", insertable = false, updatable = false)
    private User bidder;
}
