package com.shopease.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(
        @NotNull(message = "User ID is required")
        UUID userId,
        @NotEmpty(message = "Items list is required")
        List<OrderRequestItem> items
) {
}
