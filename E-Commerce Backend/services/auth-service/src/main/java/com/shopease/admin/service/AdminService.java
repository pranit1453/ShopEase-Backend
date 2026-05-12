package com.shopease.admin.service;

import com.shopease.admin.dto.AdminRequest;
import com.shopease.admin.dto.AdminResponse;
import com.shopease.admin.dto.RoleRequest;

import java.util.UUID;

public interface AdminService {
    AdminResponse onboardNewAdmin(AdminRequest request);

    void assignedNewRoleToUser(UUID userId, RoleRequest request);
}
