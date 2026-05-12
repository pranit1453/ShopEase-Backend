package com.shopease.user.entity;

import com.shopease.auditing.entity.AuditEntity;
import com.shopease.authentication.enums.Provider;
import com.shopease.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "users",
        schema = "user_schema",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends AuditEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    private String name;

    @Column(nullable = false)
    private String email;

    private String password;
    private String image;
    private String gender;
    private String oauthId;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            schema = "user_schema",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Builder.Default
    private boolean enabled = false;
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public @NullMarked Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()
                //flatMap() is used because each role produces multiple authorities.
                .flatMap(role -> Stream.concat(
                        // Role
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
                        // Permission from role
                        role.getPermissions()
                                .stream()
                                .map(permission ->
                                        new SimpleGrantedAuthority(permission.getPermissionCode()))
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public @NullMarked boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public @NullMarked boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public @NullMarked boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
