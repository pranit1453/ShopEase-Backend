package com.shopease.payment.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InvoiceDto(
        String invoiceNumber,
        String customerId,
        String paymentId,
        String email,
        BigDecimal amount,
        LocalDate paymentDate
) {
}
