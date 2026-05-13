package com.shopease.role.controller;

import com.shopease.role.dto.RoleRequest;
import com.shopease.role.dto.RoleResponse;
import com.shopease.role.dto.RoleWithPermissionRequest;
import com.shopease.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    public ResponseEntity<String> createNewRole(@RequestBody RoleRequest request) {
        roleService.createNewRole(request);
        return ResponseEntity.ok().body("Role created successfully");
    }

    @PutMapping("/update/{roleId}")
    public ResponseEntity<String> updateRole(
            @PathVariable UUID roleId,
            @RequestBody RoleRequest request
    ) {
        roleService.updateRole(roleId, request);
        return ResponseEntity.ok("Role updated successfully");
    }

    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<String> deleteRole(
            @PathVariable UUID roleId
    ) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok("Role deleted successfully");
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable UUID roleId
    ) {
        return ResponseEntity.ok(
                roleService.getRole(roleId)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    @PostMapping("/assign/{roleId}/permission")
    public ResponseEntity<String> attachPermissionToRole(
            @PathVariable UUID roleId,
            @RequestBody RoleWithPermissionRequest request) {
        roleService.assignPermissionToRole(roleId, request);
        return ResponseEntity.ok("Permission attached successfully");
    }
}
