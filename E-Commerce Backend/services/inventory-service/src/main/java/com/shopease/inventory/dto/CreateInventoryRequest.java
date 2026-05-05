package com.shopease.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateInventoryRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Total quantity is required")
        @Min(value = 0, message = "Total quantity cannot be negative")
        Integer totalQuantity,

        @NotNull(message = "Reserved quantity is required")
        @Min(value = 0, message = "Reserved quantity cannot be negative")
        Integer reservedQuantity
) {
}
