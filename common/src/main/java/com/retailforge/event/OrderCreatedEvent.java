package com.retailforge.event;

import java.util.List;

public record OrderCreatedEvent(
    Long orderId,
    String orderNumber,
    String paymentMethod,
    List<OrderItemEvent> items
) {}
