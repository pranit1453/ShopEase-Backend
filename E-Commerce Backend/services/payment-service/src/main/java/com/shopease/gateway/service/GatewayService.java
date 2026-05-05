package com.shopease.gateway.service;

import com.shopease.gateway.dto.GatewayResponse;

public interface GatewayService {
    GatewayResponse completePayment(String gatewayOrderId);
}
