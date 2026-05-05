package com.shopease.wrapper;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponse<T>(
        boolean status,
        String message,
        T data,
        Instant timestamp
) {
}
