package com.shopease.role.exception;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PermissionAreAttachedWithRole extends BaseException {
    public PermissionAreAttachedWithRole(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
