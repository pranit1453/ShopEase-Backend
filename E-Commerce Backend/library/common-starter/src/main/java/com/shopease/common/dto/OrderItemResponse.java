package com.shopease.common.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderItemResponse(
        UUID orderItemId,
        UUID productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice

) {
}
