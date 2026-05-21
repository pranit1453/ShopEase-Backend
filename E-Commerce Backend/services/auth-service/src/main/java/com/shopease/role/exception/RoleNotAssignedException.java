package com.shopease.role.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleNotAssignedException extends BaseException {
    public RoleNotAssignedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
