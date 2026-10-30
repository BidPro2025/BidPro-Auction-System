package com.bidpro.payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,String> {
    Optional<PaymentRecord> findByInvoiceID(String invoiceID);
    Optional<PaymentRecord> findByOrderID(String orderID);
}
