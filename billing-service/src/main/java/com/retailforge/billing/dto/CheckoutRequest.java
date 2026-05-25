package com.retailforge.billing.dto;

import java.util.List;

public record CheckoutRequest(
    String paymentMethod, // CASH, UPI, CARD
    List<CartItemRequest> items
) {}
