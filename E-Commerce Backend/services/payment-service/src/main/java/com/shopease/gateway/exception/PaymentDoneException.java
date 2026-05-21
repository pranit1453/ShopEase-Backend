package com.shopease.gateway.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentDoneException extends BaseException {
    public PaymentDoneException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
