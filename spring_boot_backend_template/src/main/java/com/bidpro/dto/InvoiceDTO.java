package com.bidpro.dto;

import com.bidpro.entities.Invoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceDTO extends BaseDTO {

    @Size(max = 50, message = "InvoiceID must be at most 50 characters")
    private String invoiceID;

    @NotBlank(message = "AuctionID is mandatory")
    @Size(max = 50, message = "AuctionID must be at most 50 characters")
    private String auctionID;

    @NotBlank(message = "ProductID is mandatory")
    @Size(max = 50, message = "ProductID must be at most 50 characters")
    private String productID;

    @NotBlank(message = "BuyerID is mandatory")
    @Size(max = 50, message = "BuyerID must be at most 50 characters")
    private String buyerID;

    @NotBlank(message = "SellerID is mandatory")
    @Size(max = 50, message = "SellerID must be at most 50 characters")
    private String sellerID;

    @NotBlank(message = "Amount is mandatory")
    private BigDecimal amount;

    @NotBlank(message = "Invoice date is mandatory")
    private LocalDateTime invoiceDate;

    @NotBlank(message = "Payment status is mandatory")
    private Invoice.PaymentStatus paymentStatus;
}