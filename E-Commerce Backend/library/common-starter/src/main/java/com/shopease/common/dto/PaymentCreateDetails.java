package com.shopease.common.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentCreateDetails(
        UUID orderId,
        BigDecimal amount,
        UUID userId
) {
}
