package com.shopease.payment.service;

import com.shopease.payment.dto.PaymentGatewayData;
import com.shopease.payment.dto.PaymentGatewayResponse;

public interface PaymentMethodService {
    String getServiceType();

    PaymentGatewayResponse initiate(PaymentGatewayData payment);

}
