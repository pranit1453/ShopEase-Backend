package com.shopease.redis.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OTPBlockException extends BaseException {
    public OTPBlockException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
