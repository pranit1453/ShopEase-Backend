package com.shopease.role.dto;

import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record RoleWithPermissionRequest(
        Set<UUID> permissionIds
) {
}
