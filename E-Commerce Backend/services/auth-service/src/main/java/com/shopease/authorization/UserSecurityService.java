package com.shopease.authorization;

import com.shopease.authentication.exception.AuthenticationResourceNotFound;
import com.shopease.user.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserSecurityService {

    public boolean isUserAuthorized(final @NotNull UUID userId) {
        final Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationResourceNotFound("Authentication object is null");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new AuthenticationResourceNotFound(
                    "Invalid authentication principal"
            );
        }
        final UUID uid = user.getUserId();
        return uid.equals(userId);
    }

}
