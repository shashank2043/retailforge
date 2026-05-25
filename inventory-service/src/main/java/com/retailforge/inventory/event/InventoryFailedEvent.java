package com.retailforge.inventory.event;

public record InventoryFailedEvent(
    Long orderId,
    String orderNumber,
    String reason
) {}
