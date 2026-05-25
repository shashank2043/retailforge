package com.retailforge.inventory.dto;

public record TransferStockRequest(
    Long productId,
    Long sourceWarehouseId,
    Long destinationWarehouseId,
    Integer quantity
) {}
