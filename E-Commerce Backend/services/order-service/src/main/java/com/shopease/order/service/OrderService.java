package com.shopease.order.service;

import com.shopease.common.order.dto.*;
import com.shopease.order.dto.*;
import jakarta.validation.Valid;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(@Valid OrderRequest request);

    CreateOrderResponse createOrderAndProceedToBuy(UUID userId, @Valid ProductDetails details);

    void updateOrderStatus(CreateOrderResponseToUpdateStatus request);
}
