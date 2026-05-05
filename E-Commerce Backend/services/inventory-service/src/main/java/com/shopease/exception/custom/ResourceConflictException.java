package com.shopease.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BaseException {
    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
