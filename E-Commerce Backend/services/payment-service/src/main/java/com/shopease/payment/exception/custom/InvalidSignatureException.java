package com.shopease.payment.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidSignatureException extends BaseException {
    public InvalidSignatureException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
