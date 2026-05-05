package com.shopease.authentication.dto;

import lombok.Builder;

@Builder
public record SignupResponse(
        boolean status,
        String message
) {
}
