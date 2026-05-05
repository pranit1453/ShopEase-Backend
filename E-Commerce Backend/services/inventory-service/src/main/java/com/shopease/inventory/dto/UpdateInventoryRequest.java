package com.shopease.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateInventoryRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,
        @Min(value = 1,message = "Total Quantity count should me greater than 1")
        Integer totalQuantity,
        @Min(value = 1,message = "Reserved quantity should be greater than 1")
        Integer reservedQuantity
) {
}
