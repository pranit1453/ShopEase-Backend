package com.shopease.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AssignRoleRequest(
        @NotBlank(message = "Role name is required")
        String roleName
) {
}
