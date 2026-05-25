package com.retailforge.inventory.dto;

public record InventoryResponse(
    Long id,
    Long productId,
    Long warehouseId,
    Integer quantity,
    Integer version
) {}
