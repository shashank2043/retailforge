package com.retailforge.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String barcode,
    String name,
    BigDecimal price,
    BigDecimal gstPercentage,
    CategoryResponse category
) implements Serializable {}
