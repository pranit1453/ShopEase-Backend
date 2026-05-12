package com.shopease.role.repository;

import com.shopease.role.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByRoleName(String customer);
}
