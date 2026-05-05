package com.shopease.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CreateProductRequest(
        @NotBlank(message = "Product name should not be null or empty")
        String name,
        @Length(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        @NotNull(message = "Category ID is required")
        UUID categoryId
) {
}
