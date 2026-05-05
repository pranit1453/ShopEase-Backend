package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record UpdateUserDetailsResponse(
        String username,
        String name,
        String gender
) {
}
