package com.retailforge.product.exception;

public class CategoryNotFoundException extends ProductServiceException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
