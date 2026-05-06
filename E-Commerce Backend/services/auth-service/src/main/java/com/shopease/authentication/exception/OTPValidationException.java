package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OTPValidationException extends BaseException {
    public OTPValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
