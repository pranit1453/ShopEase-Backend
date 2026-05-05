package com.shopease.authentication.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@NullMarked
public class Oauth2FailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 authentication failed", exception);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String errorCode = "Oauth2 Authentication Failed";
        String jsonResponse = """
                {
                "success": false,
                "error": %s,
                "message": %s
                "timestamp": %s
                }
                """.formatted(
                errorCode,
                exception.getMessage(),
                Instant.now().toString()
        );
        response.getWriter().write(jsonResponse);
    }
}
