package com.bidpro.dto;

import com.bidpro.entities.Auction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AuctionDTO extends BaseDTO {
    @Size(max = 50, message = "Auction ID must be at most 50 characters")
    private String auctionID;

    @NotNull(message = "Title is mandatory")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotNull(message = "Product ID is mandatory")
    @Size(max = 50, message = "Product ID must be at most 50 characters")
    private String productID;

    @NotNull(message = "Start date is mandatory")
    private LocalDateTime startDate;

    @NotNull(message = "End date is mandatory")
    private LocalDateTime endDate;

    @NotNull(message = "Starting price is mandatory")
    private BigDecimal startingPrice;

    @NotNull(message = "Status is mandatory")
    private Auction.Status status;

    @NotNull(message = "Auction type is mandatory")
    private Auction.AuctionType auctionType;

    @Size(max = 50, message = "Created by must be at most 50 characters")
    private String createdBy;

    // Default constructor
    public AuctionDTO() {}

    // Constructor that takes an Auction entity
    public AuctionDTO(Auction auction) {
        this.auctionID = auction.getAuctionID();
        this.title = auction.getTitle();
        this.productID = auction.getProductID();
        this.startDate = auction.getStartDate();
        this.endDate = auction.getEndDate();
        this.startingPrice = auction.getStartingPrice();
        this.status = auction.getStatus();
        this.auctionType = auction.getAuctionType();
        this.createdBy = auction.getCreatedBy();
    }
}
