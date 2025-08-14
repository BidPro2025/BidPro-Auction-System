package com.bidpro.dao;

import com.bidpro.entities.Invoice;
import com.bidpro.entities.Invoice.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceDao extends JpaRepository<Invoice, String> {
    List<Invoice> findByAuctionAuctionID(String auctionID);
    List<Invoice> findByBuyerUserID(String buyerID);
    List<Invoice> findBySellerUserID(String sellerID);
    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
}