package com.shopease.cart.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundInCartException extends BaseException {
    public ProductNotFoundInCartException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
