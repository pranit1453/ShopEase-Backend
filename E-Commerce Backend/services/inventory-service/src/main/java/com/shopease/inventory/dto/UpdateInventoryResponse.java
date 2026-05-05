package com.shopease.inventory.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateInventoryResponse(
        UUID inventoryId,
        UUID productId,
        Integer totalQuantity,
        Integer reservedQuantity
) {
}
