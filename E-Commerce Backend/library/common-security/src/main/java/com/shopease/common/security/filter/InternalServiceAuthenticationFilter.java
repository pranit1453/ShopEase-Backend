package com.shopease.common.security.filter;

import com.shopease.common.security.dto.InternalUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String roles = request.getHeader("X-User-Roles");
        if (userId == null || roles == null || userId.isBlank() || roles.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            UUID headerUserId;
            try {
                headerUserId = UUID.fromString(userId.trim());
            } catch (Exception e) {
                log.error("Invalid X-User-Id received: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            List<String> headerRoles = Stream.of(roles.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isBlank())
                    .toList();

            InternalUserPrincipal principal = InternalUserPrincipal.builder()
                    .userId(headerUserId)
                    .build();
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.authenticated(
                            principal,
                            null,
                            headerRoles.stream().map(SimpleGrantedAuthority::new).toList()
                    );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }
}
