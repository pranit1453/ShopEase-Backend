package com.shopease.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class CircuitBreakerProduct {

    @RequestMapping("/circuitBreakerProduct/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(
                                Map.of(
                                        "success", false,
                                        "message", "Product Service Down Contact ADMIN"
                                )
                        )
        );
    }
}
