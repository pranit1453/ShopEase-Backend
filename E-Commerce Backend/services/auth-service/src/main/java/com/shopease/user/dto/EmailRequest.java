package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record EmailRequest(
        String email
) {
}
