package com.shopease.security.filter;

import com.shopease.redis.service.RedisService;
import com.shopease.security.service.CustomUserDetailService;
import com.shopease.security.utils.JwtUtil;
import com.shopease.security.utils.SecurityResponseUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@NullMarked
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailsService;
    private final RedisService redisService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return pathMatcher.match("/api/v1/auth/register", path)
                || pathMatcher.match("/api/v1/auth/admin", path)
                || pathMatcher.match("/api/v1/auth/login", path)
                || pathMatcher.match("/api/v1/auth/logout", path)
                || pathMatcher.match("/api/v1/auth/refresh", path)
                || pathMatcher.match("/api/v1/auth/verify", path)
                || pathMatcher.match("/login/**", path)
                || pathMatcher.match("/login/oauth2/code/google", path)
                || pathMatcher.match("/login/oauth2/code/github", path)
                || pathMatcher.match("/oauth2/authorization/google", path)
                || pathMatcher.match("/oauth2/authorization/github", path);
    }

    /**
     * If Token is Valid then authentication Object is set in Security Context holder where it holds status where client is authenticated or unauthenticated
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = jwtUtil.resolveAccessToken(request);
            if (jwt != null && jwtUtil.isAccessToken(jwt) && jwtUtil.validateAccessToken(jwt)) {
                UUID userId = UUID.fromString(jwtUtil.getSubjectFromAccessToken(jwt));
                String ti = jwtUtil.getTiFromAccessToken(jwt);
                if (!redisService.verifyIdentifier(userId, ti)) {
                    SecurityResponseUtil.writeUnauthorized(
                            request,
                            response,
                            "Your session is no longer valid. Please log in again."
                    );

                    return;
                }
                UserDetails details = userDetailsService.loadUserByUserId(userId);
                if (details.isEnabled() && details.isAccountNonLocked()) {
                    UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                            details,
                            null,
                            details.getAuthorities()
                    );
                    log.debug("Roles From JWT: {}", authentication.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    if (SecurityContextHolder.getContext().getAuthentication() == null)
                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);
                }
            }
        } catch (UsernameNotFoundException e) {
            log.warn("User not found");
        } catch (JwtException e) {
            log.warn("Invalid token");
        } catch (Exception e) {
            log.error("Authentication Error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }


}
