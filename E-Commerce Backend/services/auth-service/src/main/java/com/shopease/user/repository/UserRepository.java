package com.shopease.user.repository;

import com.shopease.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions"
    })
    Optional<User> findByUserId(UUID userId);

    @Override
    @NullMarked
    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions"
    })
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(@NotBlank(message = "Username is required!!!") String username);


}
