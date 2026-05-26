package com.retailforge.product.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank(message = "Barcode is required.")
    @Size(max = 50, message = "Barcode cannot exceed 50 characters.")
    String barcode,

    @NotBlank(message = "Product name is required.")
    @Size(max = 150, message = "Product name cannot exceed 150 characters.")
    String name,

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.")
    BigDecimal price,

    @NotNull(message = "GST percentage is required.")
    @DecimalMin(value = "0.0", message = "GST percentage cannot be negative.")
    BigDecimal gstPercentage,

    @NotNull(message = "Category ID is required.")
    Long categoryId
) {}
