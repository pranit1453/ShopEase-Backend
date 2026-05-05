package com.shopease.payment.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseException {
    public PaymentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
