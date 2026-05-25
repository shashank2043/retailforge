package com.retailforge.billing.dto;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String barcode,
    String name,
    BigDecimal price,
    BigDecimal gstPercentage,
    CategoryDto category
) {}
