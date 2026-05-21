package com.shopease.role.controller;

import com.shopease.role.dto.RoleRequest;
import com.shopease.role.dto.RoleResponse;
import com.shopease.role.dto.RoleWithPermissionRequest;
import com.shopease.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_WRITE')")
    public ResponseEntity<String> createNewRole(@RequestBody RoleRequest request) {
        roleService.createNewRole(request);
        return ResponseEntity.ok().body("Role created successfully");
    }

    @PutMapping("/update/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_WRITE')")
    public ResponseEntity<String> updateRole(
            @PathVariable UUID roleId,
            @RequestBody RoleRequest request
    ) {
        roleService.updateRole(roleId, request);
        return ResponseEntity.ok("Role updated successfully");
    }

    @DeleteMapping("/delete/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_DELETE')")
    public ResponseEntity<String> deleteRole(
            @PathVariable UUID roleId
    ) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok("Role deleted successfully");
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_READ')")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(
                roleService.getRole(roleId)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_READ')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    @PostMapping("/assign/{roleId}/permission")
    @PreAuthorize("hasRole('ADMIN') and hasPermission('ROLE_WRITE')")
    public ResponseEntity<String> attachPermissionToRole(
            @PathVariable UUID roleId,
            @RequestBody RoleWithPermissionRequest request) {
        roleService.assignPermissionToRole(roleId, request);
        return ResponseEntity.ok("Permission attached successfully");
    }
}
