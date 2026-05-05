package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record VerifyPasswordRequest(
        String otp
) {
}
