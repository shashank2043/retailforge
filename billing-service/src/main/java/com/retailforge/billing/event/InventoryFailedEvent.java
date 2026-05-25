package com.retailforge.billing.event;

public record InventoryFailedEvent(
    Long orderId,
    String orderNumber,
    String reason
) {}
