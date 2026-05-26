package com.retailforge.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddStockRequest(
    @NotNull(message = "Product ID is required.")
    Long productId,

    @NotNull(message = "Warehouse ID is required.")
    Long warehouseId,

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    Integer quantity
) {}
