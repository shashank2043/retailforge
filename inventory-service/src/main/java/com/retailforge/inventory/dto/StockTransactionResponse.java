package com.retailforge.inventory.dto;

import java.time.LocalDateTime;

public record StockTransactionResponse(
    Long id,
    Long productId,
    Long warehouseId,
    Integer quantity,
    String type,
    LocalDateTime timestamp
) {}
