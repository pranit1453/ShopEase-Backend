package com.shopease.authentication.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record AssignPermissionRequest(
        @NotNull(message = "Role id is required")
        UUID roleId,
        @NotEmpty(message = "Permission code is required")
        List<String> permissionCodes
) {
}
