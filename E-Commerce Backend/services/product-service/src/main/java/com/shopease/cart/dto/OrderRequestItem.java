package com.shopease.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderRequestItem(
        @NotNull(message = "Product ID is required")
        UUID productId,
        @NotNull(message = "Quantity is required")
        Integer quantity
) {
}
