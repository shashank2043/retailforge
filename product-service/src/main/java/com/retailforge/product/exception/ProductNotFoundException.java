package com.retailforge.product.exception;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
