package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record UpdateUserDetailsRequest(
        String username,
        String name,
        String gender
) {
}
