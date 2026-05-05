package com.shopease.cart.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderRequestItem(
        UUID productId,
        Integer quantity
) {
}
