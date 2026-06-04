package com.retailforge.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
    Long orderId,
    String orderNumber,
    BigDecimal totalAmount,
    BigDecimal totalGstAmount,
    String transactionId,
    LocalDateTime completedAt,
    String customerEmail
) {}
