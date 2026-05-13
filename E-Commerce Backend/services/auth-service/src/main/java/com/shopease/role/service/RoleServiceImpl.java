package com.shopease.role.service;

import com.shopease.permission.entity.Permission;
import com.shopease.permission.exception.PermissionNotFoundException;
import com.shopease.permission.repository.PermissionRepository;
import com.shopease.role.dto.RoleRequest;
import com.shopease.role.dto.RoleResponse;
import com.shopease.role.dto.RoleWithPermissionRequest;
import com.shopease.role.entity.Role;
import com.shopease.role.exception.PermissionAreAttachedWithRole;
import com.shopease.role.exception.RoleAlreadyExistsException;
import com.shopease.role.exception.RoleNotFoundException;
import com.shopease.role.mapper.RoleMapper;
import com.shopease.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createNewRole(RoleRequest request) {
        if (roleRepository.existsByRoleName(request.roleName())) {
            throw new RoleAlreadyExistsException("Role with name " + request.roleName() + "already exists");
        }
        Role role = roleMapper.toEntity(request);
        roleRepository.save(role);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRole(UUID roleId, RoleRequest request) {
        Role role = roleRepository.findByRoleId(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role with id " + roleId + " not found"));

        Role entity = roleMapper.patchRole(request, role);
        roleRepository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRole(UUID roleId) {
        if (!roleRepository.existsByRoleId(roleId)) {
            throw new RoleNotFoundException(
                    "Role with id " + roleId + " not found"
            );
        }

        if (roleRepository.existsPermissionsByRoleId(roleId)) {
            throw new PermissionAreAttachedWithRole(
                    "Role cannot be deleted because permissions are assigned to it"
            );
        }
        roleRepository.deleteByRoleId(roleId);
    }

    @Override
    public RoleResponse getRole(UUID roleId) {
        Role role = roleRepository.findByRoleId(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role with id " + roleId + " not found"));
        return roleMapper.toResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void assignPermissionToRole(UUID roleId, RoleWithPermissionRequest request) {
        Role role = roleRepository.findByRoleId(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException("Role with id " + roleId + " not found"));

        List<Permission> permissions = permissionRepository.findAllByPermissionIdIn(request.permissionIds());

        if (permissions.size() != request.permissionIds().size()) {
            throw new PermissionNotFoundException("Some permissions are missing");
        }

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }
}
