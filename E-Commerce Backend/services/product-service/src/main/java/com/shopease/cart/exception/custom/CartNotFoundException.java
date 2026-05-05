package com.shopease.cart.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CartNotFoundException extends BaseException
{
    public CartNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
