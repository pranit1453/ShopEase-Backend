package com.shopease.admin.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleAlreadyAssignedException extends BaseException {
    public RoleAlreadyAssignedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
