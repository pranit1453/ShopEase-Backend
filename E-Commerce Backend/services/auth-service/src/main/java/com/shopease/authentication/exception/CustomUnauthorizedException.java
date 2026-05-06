package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CustomUnauthorizedException extends BaseException {
    public CustomUnauthorizedException(String message) {
        super(message,HttpStatus.UNAUTHORIZED);
    }
}
