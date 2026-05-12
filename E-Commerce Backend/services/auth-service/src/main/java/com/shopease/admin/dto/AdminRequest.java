package com.shopease.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AdminRequest(
        @NotBlank(message = "Username is required!!!")
        String username,
        @NotBlank(message = "Name is required!!!")
        String name,
        @Email
        @NotBlank(message = "Email is required!!!")
        String email,
        @NotBlank(message = "Password is required!!!")
        String password,
        @NotBlank(message = "Confirm Password is required!!!")
        String confirmPassword,
        @NotBlank(message = "Username is required!!!")
        String gender
) {
}
