package com.shopease.role.service;

import com.shopease.role.dto.RoleRequest;
import com.shopease.role.dto.RoleResponse;
import com.shopease.role.dto.RoleWithPermissionRequest;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    void createNewRole(RoleRequest request);

    void updateRole(UUID roleId, RoleRequest request);

    void deleteRole(UUID roleId);

    RoleResponse getRole(UUID roleId);

    List<RoleResponse> getAllRoles();

    void assignPermissionToRole(UUID roleId, RoleWithPermissionRequest request);
}
