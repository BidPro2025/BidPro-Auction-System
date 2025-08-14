
package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {
    @Id
    @Column(name = "productID", length = 50)
    private String productID;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sellerID", length = 50, nullable = false)
    private String sellerID;

    @Column(name = "categoryID", length = 50, nullable = false)
    private String categoryID;

    @Column(name = "auctionID", length = 50)
    private String auctionID;

    @Enumerated(EnumType.STRING)
    @Column(name = "productStatus", nullable = false, columnDefinition = "ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') DEFAULT 'PENDING'")
    private ProductStatus productStatus;

    @Column(name = "auctionStartDate")
    private LocalDateTime auctionStartDate;

    @Column(name = "auctionEndDate")
    private LocalDateTime auctionEndDate;

    @Column(name = "startingPrice", nullable = false)
    private BigDecimal startingPrice;

    @Column(name = "incrementGap", nullable = false)
    private BigDecimal incrementGap;

    @Enumerated(EnumType.STRING)
    @Column(name = "productCondition", nullable = false, columnDefinition = "ENUM('NEW', 'USED', 'REFURBISHED')")
    private ProductCondition productCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotionStatus", nullable = false, columnDefinition = "ENUM('NONE', 'PROMOTED', 'FEATURED') DEFAULT 'NONE'")
    private PromotionStatus promotionStatus;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerID", insertable = false, updatable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryID", insertable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionID", insertable = false, updatable = false)
    private Auction auction;

    public enum ProductStatus { PENDING, APPROVED, REJECTED, SOLD }
    public enum ProductCondition { NEW, USED, REFURBISHED }
    public enum PromotionStatus { NONE, PROMOTED, FEATURED }

    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(String productID, String title, String description, String sellerID, String categoryID, String auctionID,
                   ProductStatus productStatus, LocalDateTime auctionStartDate, LocalDateTime auctionEndDate,
                   BigDecimal startingPrice, BigDecimal incrementGap, ProductCondition productCondition,
                   PromotionStatus promotionStatus) {
        this.productID = productID;
        this.title = title;
        this.description = description;
        this.sellerID = sellerID;
        this.categoryID = categoryID;
        this.auctionID = auctionID;
        this.productStatus = productStatus;
        this.auctionStartDate = auctionStartDate;
        this.auctionEndDate = auctionEndDate;
        this.startingPrice = startingPrice;
        this.incrementGap = incrementGap;
        this.productCondition = productCondition;
        this.promotionStatus = promotionStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}