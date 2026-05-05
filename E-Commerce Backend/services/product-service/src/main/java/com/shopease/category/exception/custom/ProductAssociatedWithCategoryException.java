package com.shopease.category.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductAssociatedWithCategoryException extends BaseException {
    public ProductAssociatedWithCategoryException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
