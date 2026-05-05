package com.shopease.product.enums;

import lombok.Getter;

@Getter
public enum ProductSortField {
    PRODUCT_ID("productId"),
    PRICE("price");

    private final String field;

    ProductSortField(final String field) {
        this.field = field;
    }

}
