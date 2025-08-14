package com.bidpro.dto;

import com.bidpro.entities.Product;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductDTO extends BaseDTO {

    @Size(max = 50, message = "ProductID must be at most 50 characters")
    private String productID;

    @NotNull(message = "Title is mandatory")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotNull(message = "Description is mandatory")
    private String description;

    @NotNull(message = "SellerID is mandatory")
    @Size(max = 50, message = "SellerID must be at most 50 characters")
    private String sellerID;

    @NotNull(message = "CategoryID is mandatory")
    @Size(max = 50, message = "CategoryID must be at most 50 characters")
    private String categoryID;

    @Size(max = 50, message = "AuctionID must be at most 50 characters")
    private String auctionID;

    @NotNull(message = "Status is mandatory")
    private Product.ProductStatus productStatus;

    @NotNull(message = "Auction start date is mandatory")
    private LocalDateTime auctionStartDate;

    @NotNull(message = "Auction end date is mandatory")
    private LocalDateTime auctionEndDate;

    @NotNull(message = "Starting price is mandatory")
    private BigDecimal startingPrice;

    @NotNull(message = "Increment gap is mandatory")
    private BigDecimal incrementGap;

    @NotNull(message = "Condition is mandatory")
    private Product.ProductCondition productCondition;

    @NotNull(message = "Promotion status is mandatory")
    private Product.PromotionStatus promotionStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
