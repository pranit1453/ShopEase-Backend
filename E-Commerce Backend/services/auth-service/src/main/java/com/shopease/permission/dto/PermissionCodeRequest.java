package com.shopease.permission.dto;

import java.util.UUID;

public record PermissionCodeRequest(
        UUID permissionId,
        String permissionCode
) {
}
