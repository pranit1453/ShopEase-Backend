package com.shopease.authentication.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class Oauth2ProviderNotSupported extends BaseException {
    public Oauth2ProviderNotSupported(String message) {
        super(message,HttpStatus.NOT_FOUND);
    }
}
