package com.bidpro.custom_exceptions;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}