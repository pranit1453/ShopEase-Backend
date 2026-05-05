package com.shopease.authentication.dto;

import lombok.Builder;

@Builder
public record VerifyRequest(
        String email,
        String otp
) {
}
