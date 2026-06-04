package com.retailforge.event;

public record InventoryFailedEvent(
    Long orderId,
    String orderNumber,
    String reason
) {}
