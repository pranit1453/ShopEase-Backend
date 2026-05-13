package com.shopease.permission.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PermissionCodeAlreadyExists extends BaseException {
    public PermissionCodeAlreadyExists(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
