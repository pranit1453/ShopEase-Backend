package com.shopease.product.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateProductFoundException extends BaseException {
    public DuplicateProductFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
