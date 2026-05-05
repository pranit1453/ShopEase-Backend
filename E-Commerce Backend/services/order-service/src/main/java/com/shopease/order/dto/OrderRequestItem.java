package com.shopease.order.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderRequestItem(
        UUID productId,
        Integer quantity
) {
}
