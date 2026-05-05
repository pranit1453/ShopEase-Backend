package com.shopease.gateway.model;

import com.shopease.gateway.enums.GatewayOrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class GatewayOrder {
    private String gatewayOrderId;
    private BigDecimal amount;
    private GatewayOrderStatus status;
}
