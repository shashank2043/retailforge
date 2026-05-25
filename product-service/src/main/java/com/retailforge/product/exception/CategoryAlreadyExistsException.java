package com.retailforge.product.exception;

public class CategoryAlreadyExistsException extends ProductServiceException {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
