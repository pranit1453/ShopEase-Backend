package com.shopease.payment.factory;

import com.shopease.payment.service.PaymentMethodService;

public interface GatewayFactory {
    String getGatewayName();

    PaymentMethodService getPaymentService(String gateway);
}
