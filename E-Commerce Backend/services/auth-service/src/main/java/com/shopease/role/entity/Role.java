package com.shopease.role.entity;

import com.shopease.permission.entity.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles",
        schema = "user_schema",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_name", columnNames = "role_name"),
        },
        indexes = {
                @Index(name = "idx_role_name", columnList = "role_name"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;
    @Column(name = "description", length = 255)
    private String description;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            schema = "user_schema",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
