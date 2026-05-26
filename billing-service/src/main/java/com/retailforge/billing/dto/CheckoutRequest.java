package com.retailforge.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CheckoutRequest(
    @NotBlank(message = "Payment method is required.")
    String paymentMethod, // CASH, UPI, CARD

    @NotEmpty(message = "Cart must contain at least one item.")
    @Valid
    List<CartItemRequest> items
) {}
