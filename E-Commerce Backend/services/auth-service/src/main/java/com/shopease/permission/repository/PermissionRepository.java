package com.shopease.permission.repository;

import com.shopease.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByPermissionCode(String permissionCode);

    boolean existsByPermissionCode(String permissionCode);

    Optional<Permission> findByPermissionId(UUID permissionId);


    List<Permission> findAllByPermissionIdIn(Set<UUID> uuids);

    List<Permission> findByPermissionCodeIn(List<String> permissionCode);
}
