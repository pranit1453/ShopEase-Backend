package com.shopease.gateway.service;

import com.shopease.gateway.dto.GatewayResponse;
import com.shopease.gateway.enums.GatewayOrderStatus;
import com.shopease.gateway.exception.custom.InvalidGatewayOrderException;
import com.shopease.gateway.exception.custom.PaymentDoneException;
import com.shopease.gateway.model.GatewayOrder;
import com.shopease.gateway.store.DummyGatewayStore;
import com.shopease.gateway.store.FileStore;
import com.shopease.payment.custom.GatewayType;
import com.shopease.payment.dto.PaymentGatewayData;
import com.shopease.payment.dto.PaymentGatewayResponse;
import com.shopease.payment.service.PaymentMethodService;
import com.shopease.util.HmacUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@GatewayType("DUMMY")
public class UpiPaymentService implements PaymentMethodService, GatewayService {

    private final HmacUtil hmacUtil;

    @Override
    public String getServiceType() {
        return "UPI";
    }


    @Override
    public PaymentGatewayResponse initiate(PaymentGatewayData data) {
        log.info("Initiating UPI payment : orderId : {} , amount : {}", data.orderId(), data.amount());

        // verify upi id and if verified then generate the gid and g_pid and signature
        String gatewayOrderId = GenerateIDs.generateGatewayOrderId(data.orderId());
        log.debug("Generated gatewayOrderId : {}", gatewayOrderId);

        GatewayOrder gatewayOrder = GatewayOrder.builder()
                .gatewayOrderId(gatewayOrderId)
                .amount(data.amount())
                .status(GatewayOrderStatus.CREATED)
                .build();

        DummyGatewayStore.ORDERS.put(gatewayOrderId, gatewayOrder);
        log.info("Gateway order created successfully : gatewayOrderId : {}", gatewayOrderId);

        return PaymentGatewayResponse.builder()
                .gatewayOrderId(gatewayOrderId)
                .build();
    }

    @Override
    public GatewayResponse completePayment(String gatewayOrderId) {
        log.info("Completing payment : gatewayOrderId : {}", gatewayOrderId);

        GatewayOrder gatewayOrder = DummyGatewayStore.ORDERS.get(gatewayOrderId);

        if (gatewayOrder == null) {
            log.error("Invalid gateway order : gatewayOrderId : {}", gatewayOrderId);
            throw new InvalidGatewayOrderException("Invalid Order");
        }
        if (GatewayOrderStatus.PAID.equals(gatewayOrder.getStatus())) {
            log.warn("Payment already completed : gatewayOrderId : {}", gatewayOrderId);

            throw new PaymentDoneException("Payment is already paid");
        }

        String gatewayPaymentId = GenerateIDs.generateGatewayPaymentId("PAY_");
        log.debug("Generated gatewayPaymentId : {} for gatewayOrderId : {}", gatewayPaymentId, gatewayOrderId);

        String payload = gatewayOrderId + "|" + gatewayPaymentId;
        log.debug("Generating signature for payload : {}", payload);

        String generateSignature = hmacUtil.generateSignature(payload);

        gatewayOrder.setStatus(GatewayOrderStatus.PAID);
        GatewayResponse response = GatewayResponse.builder()
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .gatewaySignature(generateSignature)
                .build();

        FileStore.save(response);

        log.info("Payment completed successfully : gatewayOrderId : {} , gatewayPaymentId : {}",
                gatewayOrderId, gatewayPaymentId);
        return response;
    }
}
