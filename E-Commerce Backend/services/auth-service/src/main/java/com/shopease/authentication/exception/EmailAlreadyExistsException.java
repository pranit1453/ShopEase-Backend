package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
