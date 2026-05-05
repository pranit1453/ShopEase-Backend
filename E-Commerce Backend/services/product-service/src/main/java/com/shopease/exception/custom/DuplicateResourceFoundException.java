package com.shopease.exception.custom;

import org.springframework.http.HttpStatus;

public class DuplicateResourceFoundException extends BaseException {
    public DuplicateResourceFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
