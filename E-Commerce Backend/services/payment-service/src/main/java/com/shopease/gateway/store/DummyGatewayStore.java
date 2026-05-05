package com.shopease.gateway.store;

import com.shopease.gateway.model.GatewayOrder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DummyGatewayStore {
    /*
        For racking orders inside gateway
     */
    public static final Map<String, GatewayOrder> ORDERS = new ConcurrentHashMap<>();
}
