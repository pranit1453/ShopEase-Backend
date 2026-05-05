package com.shopease.category.dto;

import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateCategoryRequest(
        String name,
        @Length(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
