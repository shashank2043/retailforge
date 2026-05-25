package com.retailforge.inventory.event;

import java.time.LocalDateTime;

public record StockUpdatedEvent(
    Long productId,
    Long warehouseId,
    Integer oldQuantity,
    Integer newQuantity,
    LocalDateTime updatedAt
) {}
