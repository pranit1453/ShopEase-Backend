package com.shopease.authentication.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(
        String username,
        List<String> roles
) {
}
