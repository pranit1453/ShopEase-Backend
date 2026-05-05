package com.shopease.common.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CreateOrderResponseToUpdateStatus(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal grandTotal
) {
}
