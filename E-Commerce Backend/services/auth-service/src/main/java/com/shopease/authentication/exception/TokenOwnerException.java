package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TokenOwnerException extends BaseException {
    public TokenOwnerException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
