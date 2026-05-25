package com.retailforge.inventory.event;

public record OrderItemEvent(Long productId, Integer quantity) {}
