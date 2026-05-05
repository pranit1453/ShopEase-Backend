package com.shopease.payment.service;


import com.shopease.client.OrderClient;
import com.shopease.common.dto.CreateOrderResponseToUpdateStatus;
import com.shopease.payment.dto.*;
import com.shopease.payment.entity.Payment;
import com.shopease.payment.entity.PaymentGateway;
import com.shopease.payment.enums.GatewayName;
import com.shopease.payment.enums.GatewayStatus;
import com.shopease.payment.enums.PaymentMethod;
import com.shopease.payment.enums.PaymentStatus;
import com.shopease.payment.exception.custom.InvalidPaymentDetailsException;
import com.shopease.payment.exception.custom.InvalidSignatureException;
import com.shopease.payment.exception.custom.PaymentInitiationFailedException;
import com.shopease.payment.exception.custom.PaymentNotFoundException;
import com.shopease.payment.factory.PaymentGatewayFactory;
import com.shopease.payment.repository.PaymentRepository;
import com.shopease.util.HmacUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final HmacUtil hmacUtil;
    private final OrderClient orderClient;

    public Map<String, String> initiatePaymentWithGateway(PaymentInitiateRequest request) {
        log.info("Initiating payment. paymentId: {}, gateway: {}, service: {}",
                request.paymentId(), request.gateway(), request.service());
        Payment payment = paymentRepository.findByPaymentId(request.paymentId())
                .orElseThrow(() -> {
                    log.error("Payment not found. paymentId: {}", request.paymentId());
                    return new PaymentNotFoundException("Payment not found");
                });

        PaymentGatewayData paymentData = PaymentGatewayData.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .service(request.service())
                .details(request.detail())
                .build();
        log.debug("Calling payment gateway. paymentId: {}, amount: {}", payment.getPaymentId(), payment.getAmount());

        PaymentGatewayResponse response =
                paymentGatewayFactory.getGatewayFactory(request.gateway())
                        .getPaymentService(request.service())
                        .initiate(paymentData);

        if (response == null || response.gatewayOrderId() == null) {
            log.error("Payment initiation failed at gateway. paymentId: {}", payment.getPaymentId());

            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentInitiationFailedException("Payment initiation failed");
        }
        PaymentGateway gateway = PaymentGateway.builder()
                .gatewayOrderId(response.gatewayOrderId())
                .status(GatewayStatus.INITIATED)
                .gatewayName(GatewayName.valueOf(request.gateway().toUpperCase()))
                .paymentMethod(PaymentMethod.valueOf(request.service().toUpperCase()))
                .build();
        payment.attachGateway(gateway);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);
        log.info("Payment initiated successfully. paymentId: {}, gatewayOrderId: {}",
                payment.getPaymentId(), response.gatewayOrderId());
        return Map.of(
                "paymentId", payment.getPaymentId().toString(),
                "gatewayOrderId", response.gatewayOrderId()
        );
    }

    @Transactional
    public void verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment. paymentId: {}", request.paymentId());

        UUID paymentId = UUID.fromString(request.paymentId());
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found during verification. paymentId: {}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentGateway gateway = payment.getPaymentGateway();

        if (gateway == null) {
            log.error("Payment gateway not initialized. paymentId: {}", paymentId);
            throw new PaymentInitiationFailedException("Payment gateway not initialized");
        }
        String payload = request.gatewayOrderId() + "|" + request.gatewayPaymentId();
        log.debug("Generating signature for verification. paymentId: {}", paymentId);

        String generateSignature = hmacUtil.generateSignature(payload);

        if (!generateSignature.equals(request.gatewaySignature())) {
            log.error("Invalid payment signature. paymentId: {}", paymentId);

            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.getPaymentGateway().setStatus(GatewayStatus.FAILED);
            paymentRepository.save(payment);
            throw new InvalidSignatureException("Invalid Signature");
        }
        gateway.setGatewayPaymentId(request.gatewayPaymentId());
        gateway.setGatewaySignature(request.gatewaySignature());
        gateway.setStatus(GatewayStatus.VERIFIED);

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentGateway(gateway);
        paymentRepository.save(payment);
        log.info("Payment verified successfully. paymentId: {}", paymentId);

        CreateOrderResponseToUpdateStatus orderDetail = CreateOrderResponseToUpdateStatus.builder()
                .userId(payment.getUserId())
                .orderId(payment.getOrderId())
                .paymentId(payment.getPaymentId())
                .grandTotal(payment.getAmount())
                .build();
        log.info("Updating order status after payment success. orderId: {}", payment.getOrderId());

        orderClient.changeStatusOfOrder(orderDetail);
    }

    public PaymentResponse createPaymentAsOrderCreated(@Valid PaymentCreateDetails detail) {
        log.info("Creating payment for order. orderId: {}, userId: {}",
                detail.orderId(), detail.userId());
        if (detail.amount() == null ||
                detail.orderId() == null ||
                detail.userId() == null) {
            log.warn("Invalid payment details received. detail: {}", detail);
            throw new InvalidPaymentDetailsException("Invalid payment details");
        }

        Payment payment = Payment.builder()
                .orderId(detail.orderId())
                .userId(detail.userId())
                .amount(detail.amount())
                .currency("INR")
                .paymentStatus(PaymentStatus.CREATED)
                .build();


        Payment saved = paymentRepository.save(payment);
        log.info("Payment created successfully. paymentId: {}, orderId: {}",
                saved.getPaymentId(), saved.getOrderId());
        return PaymentResponse.builder()
                .orderId(saved.getOrderId())
                .paymentId(saved.getPaymentId())
                .build();
    }
}
