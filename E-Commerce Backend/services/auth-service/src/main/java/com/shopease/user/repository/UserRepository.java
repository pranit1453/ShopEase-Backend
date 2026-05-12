package com.shopease.user.repository;

import com.shopease.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions"
    })    Optional<User> findByUserId(UUID userId);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(@NotBlank(message = "Username is required!!!") String username);
    

}
