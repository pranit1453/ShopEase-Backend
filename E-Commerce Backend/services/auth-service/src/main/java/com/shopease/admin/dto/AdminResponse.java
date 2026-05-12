package com.shopease.admin.dto;

import lombok.Builder;

@Builder
public record AdminResponse(
        boolean status,
        String message
) {
}
