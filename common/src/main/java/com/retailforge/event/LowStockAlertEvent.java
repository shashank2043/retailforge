package com.retailforge.event;

import java.time.LocalDateTime;

public record LowStockAlertEvent(
    Long productId,
    Long warehouseId,
    Integer currentQuantity,
    Integer threshold,
    LocalDateTime alertedAt
) {}
