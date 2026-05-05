package com.shopease.client;

import com.shopease.payment.dto.CreateOrderResponseToUpdateStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "http://localhost:8084/api/v1/order")
public interface OrderClient {

    @PutMapping("/change-status")
    void changeStatusOfOrder(@RequestBody CreateOrderResponseToUpdateStatus request);
}
