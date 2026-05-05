package com.shopease.category.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends BaseException {
    public CategoryAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
