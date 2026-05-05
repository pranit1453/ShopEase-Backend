package com.shopease.client;

import com.shopease.cart.dto.OrderRequest;
import com.shopease.cart.dto.OrderResponse;

import com.shopease.common.dto.CreateOrderResponse;
import com.shopease.common.dto.ProductDetails;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "http://localhost:8084/api/v1/order")
@Validated
public interface OrderClient {

    @PostMapping("/create")
    OrderResponse createOrder(@RequestBody final @Valid OrderRequest request);

    @PostMapping("/create-order")
    CreateOrderResponse createOrderAndProceedToBuy(@RequestBody ProductDetails details);
}
