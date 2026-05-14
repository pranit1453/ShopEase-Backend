package com.shopease.permission.service;

import com.shopease.common.wrapper.PageResponse;
import com.shopease.permission.dto.PermissionRequest;
import com.shopease.permission.dto.PermissionResponse;
import com.shopease.permission.entity.Permission;
import com.shopease.permission.exception.PermissionCodeAlreadyExists;
import com.shopease.permission.exception.PermissionNotFoundException;
import com.shopease.permission.mapper.PermissionMapper;
import com.shopease.permission.repository.PermissionRepository;
import com.shopease.permission.specification.PermissionSpecification;
import com.shopease.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createPermission(final PermissionRequest request) {
        String moduleName = request.moduleName().toUpperCase();
        String actionName = request.actionName().toUpperCase();
        String permissionCode = moduleName + "_" + actionName;

        if (permissionRepository.existsByPermissionCode(permissionCode)) {
            throw new PermissionCodeAlreadyExists("Permission code " + permissionCode + "already exists");
        }
        Permission entity = permissionMapper.toEntity(request, permissionCode);
        permissionRepository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePermission(UUID permissionId, PermissionRequest request) {
        Permission permission = permissionRepository.findByPermissionId(permissionId)
                .orElseThrow(() ->
                        new PermissionNotFoundException("Permission with " + permissionId + "not found"));
        Permission entity = permissionMapper.patchPermission(request, permission);
        permissionRepository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deletePermission(UUID permissionId) {
        Permission permission = permissionRepository.findByPermissionId(permissionId)
                .orElseThrow(() ->
                        new PermissionNotFoundException("Permission with " + permissionId + "not found"));
        permissionRepository.delete(permission);
    }

    @Override
    public PermissionResponse getPermission(UUID permissionId) {
        Permission permission = permissionRepository.findByPermissionId(permissionId)
                .orElseThrow(() ->
                        new PermissionNotFoundException("Permission with " + permissionId + "not found"));
        return permissionMapper.toResponse(permission);
    }

    @Override
    public PageResponse<PermissionResponse> getAllPermissions(int page, int size, String keyword, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(Sort.Direction.DESC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Permission> spec = PermissionSpecification.searchKeyword(keyword);
        Page<Permission> pages = permissionRepository.findAll(spec, pageable);
        List<PermissionResponse> content = pages.getContent()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
        return PageResponse.<PermissionResponse>builder()
                .content(content)
                .page(pages.getNumber())
                .size(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }
}
