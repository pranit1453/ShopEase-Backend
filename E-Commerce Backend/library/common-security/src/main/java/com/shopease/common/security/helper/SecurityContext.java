package com.shopease.common.security.helper;

import com.shopease.common.security.dto.InternalUserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

public final class SecurityContext {
    private SecurityContext() {
    }

    public static UUID getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof InternalUserPrincipal(UUID userId)) {
            return userId;
        }
        return null;
    }

    public static List<String> getRolesFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                    .toList();
        }
        return null;
    }
}
