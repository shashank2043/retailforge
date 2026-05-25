package com.retailforge.product.dto;

import java.math.BigDecimal;

public record ProductRequest(
    String barcode,
    String name,
    BigDecimal price,
    BigDecimal gstPercentage,
    Long categoryId
) {}
