package com.shopease.permission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PermissionRequest(
        @NotBlank(message = "Module Name is required")
        String moduleName,
        @NotBlank(message = "Action Name is required")
        String actionName,
        @NotBlank(message = "Description is required")
        String description
) {
}
