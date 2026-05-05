package com.shopease.product.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductToCartRequest(
        UUID productId,
        @Min(1) Integer quantity
) {
}
