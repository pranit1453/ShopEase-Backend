package com.shopease.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
