package com.shopease.product.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateAndPlaceOrderResponse(
        UUID orderId,
        String message
) {
}
