package com.bidpro.dao;

import com.bidpro.entities.Invoice;
import com.bidpro.entities.Invoice.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceDao extends JpaRepository<Invoice, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.invoiceID = :invoiceID")
    Optional<Invoice> findForUpdateByInvoiceID(@Param("invoiceID") String invoiceID);
    List<Invoice> findByAuctionAuctionID(String auctionID);
    List<Invoice> findByBuyerUserID(String buyerID);
    List<Invoice> findBySellerUserID(String sellerID);
    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);
    Optional<Invoice> findByProductID(String productID);
    @Query("select i from Invoice i where i.buyerID = :userID or i.sellerID = :userID order by i.invoiceDate desc")
    List<Invoice> findByParty(@Param("userID") String userID);
    @Query("select i from Invoice i where i.invoiceDate between :startDate and :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
