package com.shopease.gateway.service;

import java.util.UUID;

public class GenerateIDs {

    public static String generateGatewayOrderId(UUID orderId) {
        return "order_" + System.currentTimeMillis() + "_" + orderId.toString().replace("-", "");
    }

    public static String generateGatewayPaymentId(String pay) {
        return "payment_" + System.currentTimeMillis() + "_" + pay.replace("-", "");
    }
}
