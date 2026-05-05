package com.shopease.common.dto;

import com.shopease.common.enums.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID orderId,
        UUID userId,
        UUID paymentId,
        List<OrderItemResponse> orderItemResponse,
        BigDecimal grandTotal,
        OrderStatus status,
        String message
) {
}
