package com.retailforge.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
    @NotBlank(message = "Barcode is required.")
    String barcode,

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    Integer quantity
) {}
