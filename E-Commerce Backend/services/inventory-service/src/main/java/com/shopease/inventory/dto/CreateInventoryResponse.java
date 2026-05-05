package com.shopease.inventory.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateInventoryResponse(
        UUID inventoryId,
        UUID productId,
        Integer totalQuantity,
        Integer reservedQuantity
) {
}
