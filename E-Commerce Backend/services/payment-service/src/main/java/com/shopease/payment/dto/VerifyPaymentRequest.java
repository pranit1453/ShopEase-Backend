package com.shopease.payment.dto;

import lombok.Builder;

@Builder
public record VerifyPaymentRequest(
        String paymentId,
        String gatewayOrderId,
        String gatewayPaymentId,
        String gatewaySignature
) {
}
