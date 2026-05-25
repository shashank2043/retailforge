package com.retailforge.inventory.exception;

public class InventoryNotFoundException extends InventoryServiceException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
