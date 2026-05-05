package com.shopease.authentication.dto;

import com.shopease.authentication.enums.OtpType;
import lombok.Builder;

@Builder
public record VerificationData(
        String email,
        OtpType type,
        String otp
) {
}
