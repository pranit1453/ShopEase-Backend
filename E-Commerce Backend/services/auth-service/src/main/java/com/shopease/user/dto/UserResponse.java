package com.shopease.user.dto;

import com.shopease.role.dto.RoleResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID userId,
        String name,
        String username,
        String gender,
        List<RoleResponse> roleWithPermission
) {
}
