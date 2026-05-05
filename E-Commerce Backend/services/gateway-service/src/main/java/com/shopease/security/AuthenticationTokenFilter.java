package com.shopease.security;

import com.shopease.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@NullMarked
public class AuthenticationTokenFilter implements GlobalFilter, Ordered {

    private final AccessTokenValidator accessTokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationTokenFilter(AccessTokenValidator accessTokenValidator) {
        this.accessTokenValidator = accessTokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // allow auth endpoints
        if (pathMatcher.match("/auth/api/v1/auth/**", path)) {
            return chain.filter(exchange);
        }

        try{
            String token = TokenUtils.extractToken(exchange);
            if(token == null){
                return unauthorized(exchange,"Missing token");
            }
            Claims claims = accessTokenValidator.validateToken(token);

            String userId = claims.getSubject();

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id",sanitize(userId))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        }catch (Exception e){
            return unauthorized(exchange,"Invalid or expired token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange,String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        byte[] bytes = ("{\"message\":\""+ message +"\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)
        ));
    }

    // runs early
    @Override
    public int getOrder() {
        return -1;
    }


    private String sanitize(String value) {
        return value.replaceAll("[\\n\\r\\t]", "");
    }
}
