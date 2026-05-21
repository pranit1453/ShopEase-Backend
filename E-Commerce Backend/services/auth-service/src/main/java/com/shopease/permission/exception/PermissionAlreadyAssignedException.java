package com.shopease.permission.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PermissionAlreadyAssignedException extends BaseException {
    public PermissionAlreadyAssignedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
