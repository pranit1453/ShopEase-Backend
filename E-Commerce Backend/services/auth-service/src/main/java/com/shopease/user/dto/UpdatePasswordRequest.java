package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record UpdatePasswordRequest(
        String oldPassword,
        String newPassword,
        String confirmPassword
) {
}
