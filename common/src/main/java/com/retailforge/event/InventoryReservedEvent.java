package com.retailforge.event;

public record InventoryReservedEvent(
    Long orderId,
    String orderNumber,
    String paymentMethod
) {}
