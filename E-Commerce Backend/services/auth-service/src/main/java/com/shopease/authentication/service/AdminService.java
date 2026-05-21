package com.shopease.authentication.service;

import com.shopease.authentication.dto.*;

import java.util.UUID;

public interface AdminService {
    AdminResponse onboardNewAdmin(AdminRequest request);

    void assignedNewRoleToUser(UUID userId, AssignRoleRequest request);

    void assignPermissionToRole(UUID roleId, AssignPermissionRequest request);

    void revokeRoleAssociatedToUser(UUID userId, RevokeRoleRequest request);

    void assignPermissionToUser(UUID userId, AssignPermissionRequest request);

    void revokePermissionAssociatedToUser(UUID userId, RevokePermissionRequest request);
}
