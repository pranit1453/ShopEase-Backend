package com.shopease.permission.controller;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.permission.dto.PermissionRequest;
import com.shopease.permission.dto.PermissionResponse;
import com.shopease.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<String> createPermissions(@RequestBody PermissionRequest request) {
        permissionService.createPermission(request);
        return ResponseEntity.ok().body("Permission has been created");
    }

    @PutMapping("/update/{permissionId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<String> updatePermission(
            @PathVariable UUID permissionId,
            @RequestBody PermissionRequest request
    ) {
        permissionService.updatePermission(permissionId, request);
        return ResponseEntity.ok("Permission has been updated");
    }

    @DeleteMapping("/delete/{permissionId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<String> deletePermission(
            @PathVariable UUID permissionId
    ) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.ok("Permission has been deleted");
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PermissionResponse> getPermission(
            @PathVariable UUID permissionId
    ) {
        return ResponseEntity.ok(
                permissionService.getPermission(permissionId)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PageResponse<PermissionResponse>> getAllPermissions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "permissionCode") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.ok(
                permissionService.getAllPermissions(page, size, keyword, sortBy, sortDirection)
        );
    }
}
