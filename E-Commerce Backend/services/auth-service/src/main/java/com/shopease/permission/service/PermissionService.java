package com.shopease.permission.service;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.permission.dto.PermissionRequest;
import com.shopease.permission.dto.PermissionResponse;

import java.util.UUID;

public interface PermissionService {
    void createPermission(PermissionRequest request);

    void updatePermission(UUID permissionId, PermissionRequest request);

    void deletePermission(UUID permissionId);

    PermissionResponse getPermission(UUID permissionId);

    PageResponse<PermissionResponse> getAllPermissions(int page, int size, String keyword, String sortBy, String sortDirection);
}
