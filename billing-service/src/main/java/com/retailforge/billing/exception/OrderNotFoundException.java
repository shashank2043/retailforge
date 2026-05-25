package com.retailforge.billing.exception;

public class OrderNotFoundException extends BillingServiceException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
