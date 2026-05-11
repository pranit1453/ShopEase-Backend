package com.shopease.common.security.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record InternalUserPrincipal(
        UUID userId
) {
}