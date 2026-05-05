package com.shopease.common.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID orderId,
        UUID paymentId
) {
}
