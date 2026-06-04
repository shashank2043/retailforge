package com.retailforge.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String barcode,
    String name,
    BigDecimal price,
    BigDecimal gstPercentage,
    CategoryDto category
) implements Serializable {}
