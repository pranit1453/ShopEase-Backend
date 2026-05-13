package com.shopease.role.repository;

import com.shopease.role.entity.Role;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleId(UUID roleId);

    @Query("""
            SELECT COUNT(r) > 0
            FROM Role r
            JOIN r.permissions p
            WHERE r.roleId = :roleId
            """)
    boolean existsPermissionsByRoleId(UUID roleId);

    boolean existsByRoleId(UUID roleId);

    void deleteByRoleId(UUID roleId);

    @Override
    @NullMarked
    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findAll();
}
