package com.shopease.gateway.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidGatewayOrderException extends BaseException {
    public InvalidGatewayOrderException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
