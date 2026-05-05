package com.shopease.category.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateCategoryResponse(
        UUID categoryId,
        String name,
        String description
) {
}
