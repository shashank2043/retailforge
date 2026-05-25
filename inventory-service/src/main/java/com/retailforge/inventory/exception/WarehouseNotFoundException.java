package com.retailforge.inventory.exception;

public class WarehouseNotFoundException extends InventoryServiceException {
    public WarehouseNotFoundException(String message) {
        super(message);
    }
}
