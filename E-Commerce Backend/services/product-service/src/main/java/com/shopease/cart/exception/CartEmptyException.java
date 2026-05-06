package com.shopease.cart.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CartEmptyException extends BaseException {
    public CartEmptyException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
