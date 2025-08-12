package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
public class Bid {
    @Id
    @Column(name = "bidID", length = 50)
    private String bidID;

    @Column(name = "productID", length = 50, nullable = false)
    private String productID;

    @Column(name = "bidderID", length = 50, nullable = false)
    private String bidderID;

    @Column(name = "amount", columnDefinition = "DECIMAL(10,2)", nullable = false)
    private Double amount;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

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

    // Constructors
    public Bid() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Bid(String bidID, String productID, String bidderID, Double amount, LocalDateTime timestamp) {
        this.bidID = bidID;
        this.productID = productID;
        this.bidderID = bidderID;
        this.amount = amount;
        this.timestamp = timestamp;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBidID() { return bidID; }
    public void setBidID(String bidID) { this.bidID = bidID; }
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }
    public String getBidderID() { return bidderID; }
    public void setBidderID(String bidderID) { this.bidderID = bidderID; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }
}