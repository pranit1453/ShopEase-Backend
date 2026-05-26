package com.shopease.authentication.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(
        String username,
        String accessToken,
        String refreshToken,
        List<String> roles
) {
}
