package com.shopease.category.dto;

import com.shopease.product.dto.ProductResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CategoryWithProductResponse(
        UUID categoryId,
        String name,
        String description,
        List<ProductResponse> products
) {
}
