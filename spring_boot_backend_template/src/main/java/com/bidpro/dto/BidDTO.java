package com.bidpro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.bidpro.entities.Bid;

@Getter
@Setter
public class BidDTO extends BaseDTO {
    @Size(max = 50, message = "Bid ID must be at most 50 characters")
    private String bidID;

    @NotNull(message = "Product ID is mandatory")
    @Size(max = 50, message = "Product ID must be at most 50 characters")
    private String productID;

    @NotNull(message = "Bidder ID is mandatory")
    @Size(max = 50, message = "Bidder ID must be at most 50 characters")
    private String bidderID;

    @NotNull(message = "Amount is mandatory")
    private Double amount;

    @NotNull(message = "Timestamp is mandatory")
    private LocalDateTime timestamp;

    // Default constructor
    public BidDTO() {}

    // Constructor that takes a Bid entity
    public BidDTO(Bid bid) {
        this.bidID = bid.getBidID();
        this.productID = bid.getProductID();
        this.bidderID = bid.getBidderID();
        this.amount = bid.getAmount();
        this.timestamp = bid.getTimestamp();
    }
}