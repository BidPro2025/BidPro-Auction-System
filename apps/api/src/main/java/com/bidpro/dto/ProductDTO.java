package com.bidpro.dto;

import com.bidpro.entities.Product;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class ProductDTO extends BaseDTO {
    private String productID;
    @NotBlank @Size(max = 100) private String title;
    @NotBlank @Size(max = 500) private String description;
    private String sellerID;
    @NotBlank @Size(max = 50) private String categoryID;
    private String categoryName;
    private String auctionID;
    private Product.ProductStatus productStatus;
    @NotNull private LocalDateTime auctionStartDate;
    @NotNull private LocalDateTime auctionEndDate;
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) private BigDecimal startingPrice;
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) private BigDecimal incrementGap;
    @NotNull private Product.ProductCondition productCondition;
    private Product.PromotionStatus promotionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal currentBid;
    private long bidCount;
    private List<String> imageUrls;
}
