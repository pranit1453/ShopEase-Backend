package com.shopease.authorization;

import com.shopease.authentication.exception.AuthenticationResourceNotFound;
import com.shopease.user.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserSecurityService {

    public boolean isUserAuthorized(final @NotNull UUID userId) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationResourceNotFound("Authentication object is null");
        }
        final UUID uid = ((User) Objects.requireNonNull(authentication.getPrincipal())).getUserId();

        return uid.equals(userId);
    }

}
