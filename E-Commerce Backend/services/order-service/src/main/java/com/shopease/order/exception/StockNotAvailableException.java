package com.shopease.order.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class StockNotAvailableException extends BaseException {
    public StockNotAvailableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
