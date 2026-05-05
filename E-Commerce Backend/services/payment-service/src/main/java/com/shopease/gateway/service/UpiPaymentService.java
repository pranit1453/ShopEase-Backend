package com.shopease.gateway.service;

import com.shopease.gateway.dto.GatewayResponse;
import com.shopease.gateway.enums.GatewayOrderStatus;
import com.shopease.gateway.model.GatewayOrder;
import com.shopease.gateway.store.DummyGatewayStore;
import com.shopease.gateway.store.FileStore;
import com.shopease.payment.custom.GatewayType;
import com.shopease.payment.dto.PaymentGatewayData;
import com.shopease.payment.dto.PaymentGatewayResponse;
import com.shopease.payment.service.PaymentMethodService;
import com.shopease.util.HmacUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        // verify upi id and if verified then generate the gid and g_pid and signature
        String gatewayOrderId = GenerateIDs.generateGatewayOrderId(data.orderId());
        GatewayOrder gatewayOrder = GatewayOrder.builder()
                .gatewayOrderId(gatewayOrderId)
                .amount(data.amount())
                .status(GatewayOrderStatus.CREATED)
                .build();

        DummyGatewayStore.ORDERS.put(gatewayOrderId, gatewayOrder);
        return PaymentGatewayResponse.builder()
                .gatewayOrderId(gatewayOrderId)
                .build();
    }

    @Override
    public GatewayResponse completePayment(String gatewayOrderId) {
        GatewayOrder gatewayOrder = DummyGatewayStore.ORDERS.get(gatewayOrderId);

        if (gatewayOrder == null) {
            throw new RuntimeException("Invalid Order");
        }
        if (GatewayOrderStatus.PAID.equals(gatewayOrder.getStatus())) {
            throw new RuntimeException("Payment is already paid");
        }

        String gatewayPaymentId = GenerateIDs.generateGatewayPaymentId("PAY_");
        String payload = gatewayOrderId + "|" + gatewayPaymentId;
        String generateSignature = hmacUtil.generateSignature(payload);

        gatewayOrder.setStatus(GatewayOrderStatus.PAID);
        GatewayResponse response = GatewayResponse.builder()
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .gatewaySignature(generateSignature)
                .build();

        FileStore.save(response);
        return response;
    }
}
