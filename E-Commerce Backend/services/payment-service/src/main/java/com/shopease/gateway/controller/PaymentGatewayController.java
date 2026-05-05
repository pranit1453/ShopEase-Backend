package com.shopease.gateway.controller;

import com.shopease.gateway.dto.GatewayResponse;
import com.shopease.gateway.service.GatewayService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/gateway")
@RequiredArgsConstructor
@Validated
public class PaymentGatewayController {

    private final GatewayService gatewayService;

    @PostMapping("/dummy-gateway/pay")
    public ResponseEntity<GatewayResponse> payAmount(@RequestParam @NotBlank String gatewayOrderId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(gatewayService.completePayment(gatewayOrderId));
    }
}
