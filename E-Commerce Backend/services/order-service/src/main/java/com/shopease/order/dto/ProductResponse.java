package com.shopease.order.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID productId,
        String name,
        BigDecimal price
) {
}
