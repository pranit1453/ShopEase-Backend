package com.shopease.payment.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentDetailsException extends BaseException {
    public InvalidPaymentDetailsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
