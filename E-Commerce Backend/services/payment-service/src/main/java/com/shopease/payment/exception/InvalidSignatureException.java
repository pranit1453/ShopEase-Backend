package com.shopease.payment.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidSignatureException extends BaseException {
    public InvalidSignatureException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
