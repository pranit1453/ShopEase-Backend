package com.shopease.inventory.enums;

import lombok.Getter;

@Getter
public enum InventorySortField {
    INVENTORY_ID("inventoryId"),
    PRODUCT_ID("productId");

    private final String field;

    InventorySortField(final String field) {
        this.field = field;
    }
}
