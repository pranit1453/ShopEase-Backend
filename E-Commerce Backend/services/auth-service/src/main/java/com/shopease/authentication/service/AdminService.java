package com.shopease.authentication.service;

import com.shopease.authentication.dto.AdminRequest;
import com.shopease.authentication.dto.AdminResponse;
import com.shopease.authentication.dto.RoleRequest;

import java.util.UUID;

public interface AdminService {
    AdminResponse onboardNewAdmin(AdminRequest request);

    void assignedNewRoleToUser(UUID userId, RoleRequest request);
}
