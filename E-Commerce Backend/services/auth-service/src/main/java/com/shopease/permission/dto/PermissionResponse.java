package com.shopease.permission.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PermissionResponse(
        UUID permissionId,
        String permissionCode
) {
}
