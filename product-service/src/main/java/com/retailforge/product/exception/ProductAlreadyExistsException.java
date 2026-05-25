package com.retailforge.product.exception;

public class ProductAlreadyExistsException extends ProductServiceException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
