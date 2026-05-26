package com.retailforge.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
    @NotBlank(message = "Warehouse name is required.")
    @Size(max = 100, message = "Warehouse name cannot exceed 100 characters.")
    String name,

    @NotBlank(message = "Warehouse location is required.")
    @Size(max = 200, message = "Warehouse location cannot exceed 200 characters.")
    String location
) {}
