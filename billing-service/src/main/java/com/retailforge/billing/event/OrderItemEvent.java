package com.retailforge.billing.event;

public record OrderItemEvent(Long productId, Integer quantity) {}
