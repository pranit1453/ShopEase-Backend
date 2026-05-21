package com.shopease.authentication.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record RevokeRoleRequest(
        @NotEmpty(message = "Role id is required")
        UUID roleId
) {
}
