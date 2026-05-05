package com.shopease.common.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderRequestItem(
        UUID productId,
        Integer quantity
) {
}
