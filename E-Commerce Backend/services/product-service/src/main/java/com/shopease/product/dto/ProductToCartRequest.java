package com.shopease.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductToCartRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        @NotNull(message = "Quantity is required")
        @Min(1) Integer quantity
) {
}
