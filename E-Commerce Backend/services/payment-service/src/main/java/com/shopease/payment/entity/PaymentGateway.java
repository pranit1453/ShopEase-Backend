package com.shopease.payment.entity;

import com.shopease.payment.enums.GatewayName;
import com.shopease.payment.enums.GatewayStatus;
import com.shopease.payment.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_gateways",
        schema = "payment_schema",
        indexes = {
                @Index(name = "idx_gateway_payment_id", columnList = "gatewayPaymentId"),
                @Index(name = "idx_gateway_order_id", columnList = "gatewayOrderId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentGateway {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID gatewayId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayName gatewayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String gatewayOrderId;

    @Column(unique = true)
    private String gatewayPaymentId;

    private String gatewaySignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayStatus status;

    @OneToOne
    @JoinColumn(
            name = "payment_id",
            nullable = false)
    private Payment payment;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.status = GatewayStatus.INITIATED;

    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
