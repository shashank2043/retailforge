package com.retailforge.billing.exception;

public class ProductNotFoundException extends BillingServiceException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
