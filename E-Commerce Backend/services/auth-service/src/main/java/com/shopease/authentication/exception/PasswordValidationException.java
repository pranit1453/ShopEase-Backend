package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PasswordValidationException extends BaseException {
    public PasswordValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
