package com.shopease.payment.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentInitiationFailedException extends BaseException {
    public PaymentInitiationFailedException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
