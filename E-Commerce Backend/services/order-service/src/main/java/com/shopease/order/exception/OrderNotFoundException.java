package com.shopease.order.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseException {

    public OrderNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
