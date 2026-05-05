package com.shopease.service;

import com.shopease.common.dto.OrderDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OrderCreationNotificationService {

    private final EmailService emailService;

    @Bean
    public Function<OrderDetails, UUID> orderCreatedNotification() {
        // logic to send email to user
        return (this::handleOrderCreated);
    }

    private UUID handleOrderCreated(final OrderDetails orderDetails) {
        String to = "pranitbhangale07@gmail.com";
        String subject = "Order Created Notification";
        String body = buildEmailBody(orderDetails);

        emailService.sendEmail(to, subject, body);
        return orderDetails.orderId();
    }

    private String buildEmailBody(final OrderDetails orderDetails) {
        return """
                                    ORDER CONFIRMATION RECEIPT
                
                Hello %s,
                
                ✅ Payment Successful
                ✅ Order Placed Successfully
                
                Your order has been confirmed and is now being processed.
                
                --------------------------------------------------------------
                ORDER DETAILS
                --------------------------------------------------------------
                Order ID      : %s
                Payment ID    : %s
                User ID       : %s
                
                Products:
                %s
                
                --------------------------------------------------------------
                PAYMENT SUMMARY
                --------------------------------------------------------------
                Grand Total   : ₹%s
                
                --------------------------------------------------------------
                SHIPPING STATUS
                --------------------------------------------------------------
                Status        : Processing
                Estimated     : Will be updated soon
                
                **************************************************************
                Thank you for shopping with us!
                We appreciate your purchase and hope to serve you again.
                """.formatted(
                orderDetails.userId(),
                orderDetails.orderId(),
                orderDetails.paymentId(),
                orderDetails.userId(),
                orderDetails.productName().stream()
                        .map(product -> " • " + product)
                        .reduce("", (a, b) -> a + "\n" + b),
                orderDetails.grandTotal()
        );
    }
}
