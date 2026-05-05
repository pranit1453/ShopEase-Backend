package com.shopease.order.client;

import com.shopease.order.dto.PaymentCreateDetails;
import com.shopease.order.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "http://localhost:8085/api/v1/payment")
public interface PaymentClient {

    @PostMapping("/create-payment")
    PaymentResponse createPayment(@RequestBody PaymentCreateDetails paymentCreateDetails);
}
