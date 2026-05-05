package com.shopease.order.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateOrderResponse(
        UUID orderId,
        UUID paymentId,
        String message
) {
}
