package com.shopease.common.wrapper;

import lombok.Builder;

import java.time.Instant;
/**
 * Standardized error response wrapper used to return consistent API error details.
 * Contains HTTP status, error message, request path, and timestamp.
 */
@Builder
public record ErrorResponse(
        int status,
        String message,
        String path,
        Instant timestamp
) {
}
