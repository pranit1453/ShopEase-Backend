package com.shopease.dto;

import com.shopease.common.enums.OtpType;
import lombok.Builder;

@Builder
public record VerificationData(
        String email,
        OtpType type,
        String otp
) {
}
