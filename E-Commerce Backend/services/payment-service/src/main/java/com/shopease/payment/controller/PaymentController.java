package com.shopease.payment.controller;

import com.shopease.payment.dto.PaymentCreateDetails;
import com.shopease.payment.dto.PaymentInitiateRequest;
import com.shopease.payment.dto.PaymentResponse;
import com.shopease.payment.dto.VerifyPaymentRequest;
import com.shopease.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid PaymentCreateDetails details) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.createPaymentAsOrderCreated(details));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> initiatePayment(
            @RequestBody @Valid PaymentInitiateRequest request

    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.initiatePaymentWithGateway(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody @Valid VerifyPaymentRequest request
    ) {
        paymentService.verifyPayment(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Payment Verified");
    }
}
