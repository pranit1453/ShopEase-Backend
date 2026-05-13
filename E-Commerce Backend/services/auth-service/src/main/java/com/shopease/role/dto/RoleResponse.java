package com.shopease.role.dto;

import com.shopease.permission.dto.PermissionResponse;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record RoleResponse(
        UUID roleId,
        String roleName,
        String description,
        Set<PermissionResponse> permissions
) {
}
