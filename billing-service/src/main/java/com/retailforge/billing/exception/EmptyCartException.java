package com.retailforge.billing.exception;

public class EmptyCartException extends BillingServiceException {
    public EmptyCartException(String message) {
        super(message);
    }
}
