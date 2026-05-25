package com.retailforge.inventory.exception;

public class InvalidQuantityException extends InventoryServiceException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
