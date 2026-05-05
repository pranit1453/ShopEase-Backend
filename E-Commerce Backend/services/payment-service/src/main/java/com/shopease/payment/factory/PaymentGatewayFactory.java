package com.shopease.payment.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

    private final Map<String, GatewayFactory> gatewayFactoryMap;

    public PaymentGatewayFactory(List<GatewayFactory> factories) {
        this.gatewayFactoryMap = factories.stream()
                .collect(Collectors.toMap(
                        f -> f.getGatewayName().toUpperCase(),
                        Function.identity()
                ));
    }

    public GatewayFactory getGatewayFactory(String gateway) {
        return Optional.ofNullable(gatewayFactoryMap.get(gateway.toUpperCase()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Gateway " + gateway + " not found."));
    }
}
