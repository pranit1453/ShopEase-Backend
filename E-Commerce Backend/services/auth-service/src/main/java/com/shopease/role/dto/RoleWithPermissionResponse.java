package com.shopease.role.dto;

import com.shopease.permission.dto.PermissionCodeResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RoleWithPermissionResponse(
        UUID roleId,
        String roleName,
        List<PermissionCodeResponse> permissions
) {
}
