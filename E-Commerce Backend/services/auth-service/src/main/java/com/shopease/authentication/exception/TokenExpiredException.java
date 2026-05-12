package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseException {
    public TokenExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
