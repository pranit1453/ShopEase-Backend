package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AccountVerificationException extends BaseException {

    public AccountVerificationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
