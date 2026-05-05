package com.shopease.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentCreateDetails(
        @NotNull
        UUID orderId,

        @NotNull
        UUID userId,

        @NotNull
        BigDecimal amount
) {
}
