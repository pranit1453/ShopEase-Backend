package com.shopease.admin.controller;

import com.shopease.admin.dto.AdminRequest;
import com.shopease.admin.dto.AdminResponse;
import com.shopease.admin.dto.RoleRequest;
import com.shopease.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/onboard")
    public ResponseEntity<AdminResponse> adminOnBoarding(final @RequestBody AdminRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.onboardNewAdmin(request));
    }

    @PatchMapping("/assign-new-role")
    @PreAuthorize("(hasRole('ADMIN')")
    public ResponseEntity<String> changeRole(final UUID userId, final @RequestBody RoleRequest request) {
        adminService.assignedNewRoleToUser(userId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("New Role has been assigned to the user");
    }
}
