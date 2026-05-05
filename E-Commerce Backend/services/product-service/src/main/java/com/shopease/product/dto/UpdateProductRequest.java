package com.shopease.product.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Builder
public record UpdateProductRequest(
        String name,
        @Length(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price
) {
}
