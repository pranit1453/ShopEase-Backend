package com.shopease.cart.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(
        UUID userId,
        List<OrderRequestItem> items
) {
}
