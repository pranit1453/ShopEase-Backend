package com.shopease.wishlist.exception.custom;

import com.shopease.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class WishlistDuplicateResourceFoundException extends BaseException {
    public WishlistDuplicateResourceFoundException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
