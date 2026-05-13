package com.shopease.role.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class RoleAlreadyExistsException extends BaseException {
    public RoleAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
