package com.retailforge.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransferStockRequest(
    @NotNull(message = "Product ID is required.")
    Long productId,

    @NotNull(message = "Source warehouse ID is required.")
    Long sourceWarehouseId,

    @NotNull(message = "Destination warehouse ID is required.")
    Long destinationWarehouseId,

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    Integer quantity
) {}
