package com.shopease.order.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID orderId,
        UUID paymentId
) {
}
