package com.retailforge.event;

public record OrderItemEvent(Long productId, Integer quantity) {}
