package com.shopease.category.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryResponse(
        UUID categoryId,
        String name,
        String description
) {
}
