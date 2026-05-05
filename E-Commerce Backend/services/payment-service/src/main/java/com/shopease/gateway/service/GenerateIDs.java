package com.shopease.gateway.service;

import java.util.UUID;

public class GenerateIDs {

    public static String generateGatewayOrderId(UUID orderId) {
        return "ORDER_" + System.currentTimeMillis() + "_" + orderId.toString().replace("-", "");
    }

    public static String generateGatewayPaymentId(String pay) {
        return pay
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }
}
