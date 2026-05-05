package com.shopease.payment.factory;

import com.shopease.payment.custom.GatewayType;
import com.shopease.payment.service.PaymentMethodService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DummyGatewayFactory implements GatewayFactory {

    private final Map<String, PaymentMethodService> serviceMap;

    public DummyGatewayFactory(List<PaymentMethodService> services) {
        this.serviceMap = services.stream()
                .filter(s -> {
                    GatewayType annotation = s.getClass().getAnnotation(GatewayType.class);
                    return annotation != null &&
                            "DUMMY".equalsIgnoreCase(annotation.value());
                })
                .collect(Collectors.toMap(
                        PaymentMethodService::getServiceType,
                        Function.identity()
                ));
    }

    @Override
    public String getGatewayName() {
        return "DUMMY";
    }

    @Override
    public PaymentMethodService getPaymentService(String service) {
        return Optional.ofNullable(serviceMap.get(service.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Service " + service + " not supported for gateway DUMMY"));
    }
}
