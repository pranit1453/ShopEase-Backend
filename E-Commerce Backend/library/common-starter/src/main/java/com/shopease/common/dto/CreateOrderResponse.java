package com.shopease.common.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateOrderResponse(
        UUID orderId,
        String message
) {
}
