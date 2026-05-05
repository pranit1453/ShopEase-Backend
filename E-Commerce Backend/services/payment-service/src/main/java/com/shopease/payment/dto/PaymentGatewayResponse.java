package com.shopease.payment.dto;

import lombok.Builder;

@Builder
public record PaymentGatewayResponse(
        String gatewayOrderId
) {
}
