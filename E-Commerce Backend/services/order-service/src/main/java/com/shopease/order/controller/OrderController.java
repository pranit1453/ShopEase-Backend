package com.shopease.order.controller;

import com.shopease.order.dto.*;
import com.shopease.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody final @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrderAndProceedToBuy(@RequestBody final @Valid ProductDetails details) {
        UUID userId = UUID.fromString("3a6bea85-1f4e-4a77-b3d6-13ab65f06589");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrderAndProceedToBuy(userId, details));
    }

    @PutMapping("/change-status")
    public void changeStatusOfOrder(@RequestBody CreateOrderResponseToUpdateStatus request) {
        orderService.updateOrderStatus(request);
    }
}
