package com.shopease.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateResourceFoundException extends BaseException {
    public DuplicateResourceFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
