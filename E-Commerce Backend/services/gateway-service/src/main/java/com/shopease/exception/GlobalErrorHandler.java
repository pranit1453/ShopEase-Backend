package com.shopease.exception;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
@NullMarked
public class GlobalErrorHandler implements org.springframework.boot.webflux.error.ErrorWebExceptionHandler {

    private static String getMessage(Throwable ex) {
        String message = "Something went wrong";
        String serviceName = null;
        if (ex instanceof NotFoundException) {
            String exMessage = ex.getMessage();

            if (exMessage.contains("Unable to find instance for")) {
                serviceName = exMessage.replace("503 SERVICE_UNAVAILABLE \"Unable to find instance for ", "")
                        .replace("\"", "")
                        .trim();
            }

            message = (serviceName != null)
                    ? serviceName + " is currently unavailable"
                    : "Requested service is unavailable";
        }
        return message;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String message = getMessage(ex);
        String response = """
                {
                    "status": 503,
                    "message": "%s"
                }
                """.formatted(message);

        byte[] responseBody = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory().wrap(responseBody)));
    }
}
