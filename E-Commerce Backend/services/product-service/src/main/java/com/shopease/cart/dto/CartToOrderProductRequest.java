package com.shopease.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CartToOrderProductRequest(
        @NotNull(message = "User ID is required")
        UUID userId,
        @NotNull(message = "Cart ID is required")
        UUID cartId
) {
}
