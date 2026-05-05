package com.shopease.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductDetails(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        Integer quantity
) {
}
