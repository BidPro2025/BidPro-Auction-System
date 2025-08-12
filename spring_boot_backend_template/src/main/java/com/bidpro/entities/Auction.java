package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "auction")
public class Auction {
    @Id
    @Column(name = "auctionID", length = 50)
    private String auctionID;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "productID", length = 50, nullable = false)
    private String productID;

    @Column(name = "startDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "startingPrice", nullable = false)
    private BigDecimal startingPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('UPCOMING', 'ONGOING', 'PAST')")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auctionType", nullable = false, columnDefinition = "ENUM('STANDARD', 'REVERSE')")
    private AuctionType auctionType;

    @Column(name = "createdBy", length = 50)
    private String createdBy;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", insertable = false, updatable = false)
    private Admin createdByAdmin;

    public enum Status { UPCOMING, ONGOING, PAST }
    public enum AuctionType { STANDARD, REVERSE }

    public Auction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Auction(String auctionID, String title, String productID, LocalDateTime startDate, LocalDateTime endDate, BigDecimal startingPrice, Status status, AuctionType auctionType, String createdBy) {
        this.auctionID = auctionID;
        this.title = title;
        this.productID = productID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startingPrice = startingPrice;
        this.status = status;
        this.auctionType = auctionType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
