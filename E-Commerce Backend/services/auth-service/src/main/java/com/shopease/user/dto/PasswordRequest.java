package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record PasswordRequest(
        String newPassword,
        String confirmPassword
) {
}
