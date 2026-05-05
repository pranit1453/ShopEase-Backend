package com.shopease.payment.dto;

import com.shopease.payment.details.PaymentDetails;

import java.util.UUID;

public record PaymentInitiateRequest(
        UUID paymentId,
        String gateway,
        String service,
        PaymentDetails detail
) {
}
