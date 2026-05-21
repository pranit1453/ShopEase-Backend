package com.shopease.payment.controller;

import com.shopease.payment.dto.*;
import com.shopease.payment.service.PaymentService;
import com.shopease.payment.service.PdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final PdfService pdfService;

    @PostMapping("/create-payment")
    @PreAuthorize("hasRole('USER') and hasAuthority('PAYMENT_WRITE')")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid PaymentCreateDetails details) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.createPaymentAsOrderCreated(details));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER') and hasAuthority('PAYMENT_WRITE')")
    public ResponseEntity<Map<String, String>> initiatePayment(
            @RequestBody @Valid PaymentInitiateRequest request

    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.initiatePaymentWithGateway(request));
    }

    @PostMapping("/verify")
    // @PreAuthorize("hasRole('USER') and hasAuthority('PAYMENT_WRITE')")
    public ResponseEntity<String> verifyPayment(
            @RequestBody @Valid VerifyPaymentRequest request
    ) {
        paymentService.verifyPayment(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Payment Verified");
    }

    @GetMapping("/invoice")
    public ResponseEntity<InputStreamResource> getInvoice() {
        InvoiceDto data = InvoiceDto.builder()
                .invoiceNumber(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .paymentId(UUID.randomUUID().toString())
                .email("pranit@123gmail.com")
                .amount(BigDecimal.valueOf(45000.00))
                .paymentDate(LocalDate.now())
                .build();

        ByteArrayInputStream pdf = pdfService.generateInvoice(data);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}
