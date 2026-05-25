package com.retailforge.billing.event;

public record InventoryReservedEvent(
    Long orderId,
    String orderNumber,
    String paymentMethod
) {}
