package com.shopease.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 1. Purpose:
 - Acts as a parent class for all custom exceptions
 - Standardizes error handling across the application

 2. Flow:

 Exception thrown → caught by GlobalExceptionHandler →
 HTTP response created using:
 - message
 - status

 A reusable base class for consistent and structured error handling
 */
@Getter
public class BaseException extends RuntimeException {
    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
