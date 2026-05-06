package com.shopease.redis.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OTPResendException extends BaseException {
    public OTPResendException(String message) {
        super(message, HttpStatus.TOO_EARLY);
    }
}
