package com.retailforge.billing.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
    Long orderId,
    String orderNumber,
    BigDecimal totalAmount,
    String status,
    String paymentStatus,
    String transactionId,
    String invoiceNumber
) {}
