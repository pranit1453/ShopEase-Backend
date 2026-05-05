package com.shopease.category.enums;

import lombok.Getter;

@Getter
public enum CategorySortField {
    CATEGORY_ID("categoryId"),
    NAME("name");

    private final String field;

    CategorySortField(final String field) {
        this.field = field;
    }
}
