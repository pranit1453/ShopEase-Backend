package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record NewEmailRequest(
        String newEmail,
        String otp
) {
}
