package com.shopease.permission;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permissions"
        , schema = "user_schema",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_code", columnNames = "permission_code"),
        },
        indexes = {
                @Index(name = "idx_permission_code", columnList = "permission_code"),
                @Index(name = "idx_permission_module", columnList = "module_name"),
                @Index(name = "idx_permission_action", columnList = "action_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", nullable = false, updatable = false)
    private UUID permissionId;

    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;
    @Column(name = "module_name")
    private String moduleName;
    @Column(name = "action_name")
    private String actionName;
    @Column(name = "description", length = 255)
    private String description;
}
