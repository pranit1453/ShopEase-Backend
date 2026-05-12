package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyExists extends BaseException {

    public UsernameAlreadyExists(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
