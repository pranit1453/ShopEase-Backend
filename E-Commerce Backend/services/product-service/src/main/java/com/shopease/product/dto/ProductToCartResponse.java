package com.shopease.product.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductToCartResponse(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice
) {
}
