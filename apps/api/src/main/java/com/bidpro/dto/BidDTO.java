package com.bidpro.dto;

import com.bidpro.entities.Bid;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BidDTO extends BaseDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String bidID;
    @Size(max = 50)
    private String productID;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String bidderID;
    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Bid amount must be positive")
    @Digits(integer = 8, fraction = 2, message = "Bid amount supports up to 8 digits and 2 decimal places")
    private BigDecimal amount;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime timestamp;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productTitle;

    public BidDTO() {}
    public BidDTO(Bid bid) {
        bidID = bid.getBidID();
        productID = bid.getProductID();
        bidderID = bid.getBidderID();
        amount = bid.getAmount();
        timestamp = bid.getTimestamp();
    }
}
