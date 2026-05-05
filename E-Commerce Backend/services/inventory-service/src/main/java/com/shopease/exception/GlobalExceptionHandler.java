package com.shopease.exception;

import com.shopease.common.exception.BaseException;
import com.shopease.common.wrapper.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.shopease.common.exception.BaseException.class)
    public ResponseEntity<ErrorResponse<Object>> handleBaseException(BaseException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.builder()
                        .status(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(false)
                        .message(message)
                        .data(null)
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {

        String message = "Data integrity violation. Please ensure the request is valid.";

        Throwable root = ex.getRootCause();

        if (root instanceof org.hibernate.exception.ConstraintViolationException constraintEx) {

            String constraintName = constraintEx.getConstraintName();

            if (constraintName != null) {
                if (constraintName.contains("uk") || constraintName.contains("unique")) {
                    message = "Duplicate resource found. Resource already exists.";
                } else if (constraintName.contains("fk")) {
                    message = "Invalid reference. Related resource does not exist.";
                } else if (constraintName.contains("not_null")) {
                    message = "Missing required field.";
                }
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(false)
                        .message(message)
                        .data(null)
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(Instant.now())
                        .build());
    }
}
