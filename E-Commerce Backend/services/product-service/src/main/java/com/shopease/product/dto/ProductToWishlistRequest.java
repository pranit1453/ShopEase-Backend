package com.shopease.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductToWishlistRequest(
        @NotNull(message = "Product ID is required")
        UUID productId
) {
}
