package com.shopease.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RoleRequest(
        @NotBlank(message = "Role to assigned is mandatory")
        String role
) {
}
