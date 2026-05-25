package com.retailforge.inventory.event;

public record InventoryReservedEvent(
    Long orderId,
    String orderNumber,
    String paymentMethod
) {}
