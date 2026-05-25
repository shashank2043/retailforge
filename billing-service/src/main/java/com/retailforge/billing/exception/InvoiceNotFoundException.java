package com.retailforge.billing.exception;

public class InvoiceNotFoundException extends BillingServiceException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
