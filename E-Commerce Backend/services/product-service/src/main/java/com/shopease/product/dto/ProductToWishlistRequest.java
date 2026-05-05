package com.shopease.product.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductToWishlistRequest(
        UUID productId
) {
}
