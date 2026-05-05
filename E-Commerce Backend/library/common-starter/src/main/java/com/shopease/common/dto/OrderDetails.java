package com.shopease.common.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderDetails(
        UUID userId,
        UUID orderId,
        UUID paymentId,
        List<String> productName,
        BigDecimal grandTotal
) {
}
