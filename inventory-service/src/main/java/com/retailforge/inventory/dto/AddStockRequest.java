package com.retailforge.inventory.dto;

public record AddStockRequest(
    Long productId,
    Long warehouseId,
    Integer quantity
) {}
