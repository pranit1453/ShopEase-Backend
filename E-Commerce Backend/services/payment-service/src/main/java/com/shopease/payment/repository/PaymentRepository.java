package com.shopease.payment.repository;

import com.shopease.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByPaymentId(UUID paymentId);

    Payment findByPaymentGateway_GatewayOrderId(String paymentGatewayGatewayOrderId);

    Payment findByPaymentGateway_GatewayOrderIdAndPaymentGateway_GatewayPaymentId(String paymentGatewayGatewayOrderId, String paymentGatewayGatewayPaymentId);
}
