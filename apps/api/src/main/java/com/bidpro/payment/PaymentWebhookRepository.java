package com.bidpro.payment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhookEvent,String> {}
