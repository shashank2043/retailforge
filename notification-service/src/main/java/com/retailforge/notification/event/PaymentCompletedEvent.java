package com.retailforge.notification.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
    Long orderId,
    String orderNumber,
    BigDecimal totalAmount,
    String transactionId,
    LocalDateTime completedAt
) {}
