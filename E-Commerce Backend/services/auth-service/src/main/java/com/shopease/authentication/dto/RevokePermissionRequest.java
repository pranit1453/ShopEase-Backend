package com.shopease.authentication.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record RevokePermissionRequest(
        @NotEmpty(message = "Permission codes cannot be empty")
        List<String> permissionCodes
) {
}
