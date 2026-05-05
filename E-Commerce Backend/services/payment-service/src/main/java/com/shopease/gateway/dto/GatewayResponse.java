package com.shopease.gateway.dto;

import lombok.Builder;

@Builder
public record GatewayResponse(
        String gatewayPaymentId,
        String gatewayOrderId,
        String gatewaySignature
) {
}
