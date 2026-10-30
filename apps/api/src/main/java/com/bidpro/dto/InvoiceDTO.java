package com.bidpro.dto;

import com.bidpro.entities.Invoice;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceDTO extends BaseDTO {
    private String invoiceID;
    private String auctionID;
    private String productID;
    private String buyerID;
    private String sellerID;
    private BigDecimal amount;
    private LocalDateTime invoiceDate;
    private Invoice.PaymentStatus paymentStatus;
    private Invoice.PaymentSource paymentSource;
    private String paymentReference;
    private String paymentMode;
    private boolean hasOnlinePaymentOrder;
    private String productTitle;
    private String buyerName;
    private String sellerName;

    public InvoiceDTO() {}
    public InvoiceDTO(Invoice invoice) {
        invoiceID = invoice.getInvoiceID();
        auctionID = invoice.getAuctionID();
        productID = invoice.getProductID();
        buyerID = invoice.getBuyerID();
        sellerID = invoice.getSellerID();
        amount = invoice.getAmount();
        invoiceDate = invoice.getInvoiceDate();
        paymentStatus = invoice.getPaymentStatus();
        paymentSource = invoice.getPaymentSource() == null ? (invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID ? Invoice.PaymentSource.MANUAL : Invoice.PaymentSource.NONE) : invoice.getPaymentSource();
        paymentReference = invoice.getPaymentReference();
    }
}
