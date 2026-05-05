package com.shopease.product.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID productId,
        String name,
        String description,
        BigDecimal price
) {
}
