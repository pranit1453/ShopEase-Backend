package com.shopease.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VerifyRequest(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required!!!")
        String email,
        @NotBlank(message = "OTP is required!!!")
        String otp
) {
}
