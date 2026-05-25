package com.retailforge.inventory.exception;

public class InvalidWarehouseTransferException extends InventoryServiceException {
    public InvalidWarehouseTransferException(String message) {
        super(message);
    }
}
