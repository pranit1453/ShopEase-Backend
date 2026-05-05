package com.shopease.user.dto;

import lombok.Builder;

@Builder
public record EmailData(
        String oldEmail,
        String newEmail
) {
}
