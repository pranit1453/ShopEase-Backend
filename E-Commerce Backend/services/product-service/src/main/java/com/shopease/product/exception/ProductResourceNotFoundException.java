package com.shopease.product.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductResourceNotFoundException extends BaseException {
    public ProductResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
