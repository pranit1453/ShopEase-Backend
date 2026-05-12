package com.shopease.authentication.dto;

import lombok.Builder;

@Builder
public record AdminResponse(
        boolean status,
        String message
) {
}
