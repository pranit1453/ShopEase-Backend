package com.shopease.exception.custom;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BaseException {
    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
