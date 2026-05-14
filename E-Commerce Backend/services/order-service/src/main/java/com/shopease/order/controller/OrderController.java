package com.shopease.order.controller;

import com.shopease.common.security.helper.SecurityContext;
import com.shopease.order.dto.*;
import com.shopease.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') and hasAuthority('ORDER_WRITE')")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody final @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('USER') and hasAuthority('ORDER_WRITE')")
    public ResponseEntity<CreateOrderResponse> createOrderAndProceedToBuy(@RequestBody final @Valid ProductDetails details) {
        UUID userId = SecurityContext.getUserIdFromSecurityContext();
        log.debug("UserID in Security Context: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrderAndProceedToBuy(userId, details));
    }

    @PutMapping("/change-status")
    @PreAuthorize("hasRole('USER') and hasAuthority('ORDER_WRITE')")
    public void changeStatusOfOrder(@RequestBody CreateOrderResponseToUpdateStatus request) {
        orderService.updateOrderStatus(request);
    }
}
