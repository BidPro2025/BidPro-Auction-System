package com.bidpro.payment;
public class PaymentUnavailableException extends RuntimeException {
    public PaymentUnavailableException(String message) { super(message); }
}
