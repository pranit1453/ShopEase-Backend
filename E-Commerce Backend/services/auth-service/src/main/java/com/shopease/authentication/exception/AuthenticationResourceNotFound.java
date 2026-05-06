package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AuthenticationResourceNotFound extends BaseException {
    public AuthenticationResourceNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
