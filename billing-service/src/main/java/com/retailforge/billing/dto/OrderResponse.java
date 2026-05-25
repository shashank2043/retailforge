package com.retailforge.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {}
