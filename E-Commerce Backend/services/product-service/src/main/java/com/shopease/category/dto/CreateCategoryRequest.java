package com.shopease.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record CreateCategoryRequest(
        @NotBlank(message = "Category name should not be null or empty")
        String name,
        @Length(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
