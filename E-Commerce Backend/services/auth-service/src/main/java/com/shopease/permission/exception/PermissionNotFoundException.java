package com.shopease.permission.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PermissionNotFoundException extends BaseException {
    public PermissionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
