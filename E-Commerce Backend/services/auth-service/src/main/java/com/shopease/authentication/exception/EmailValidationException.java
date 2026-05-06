package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EmailValidationException extends BaseException {
    public EmailValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
