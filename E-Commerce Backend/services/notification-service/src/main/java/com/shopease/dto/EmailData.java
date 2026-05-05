package com.shopease.dto;

import lombok.Builder;

@Builder
public record EmailData(
        String oldEmail,
        String newEmail
) {
}
