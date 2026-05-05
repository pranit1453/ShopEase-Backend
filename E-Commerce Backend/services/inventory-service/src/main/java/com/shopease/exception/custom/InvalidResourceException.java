package com.shopease.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidResourceException extends BaseException {
    public InvalidResourceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
