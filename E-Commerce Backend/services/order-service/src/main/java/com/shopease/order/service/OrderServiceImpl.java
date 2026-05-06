package com.shopease.order.service;

import com.shopease.order.client.InventoryClient;
import com.shopease.order.client.PaymentClient;
import com.shopease.order.client.ProductClient;
import com.shopease.order.dto.*;
import com.shopease.order.entity.Order;
import com.shopease.order.entity.OrderItem;
import com.shopease.order.enums.OrderStatus;
import com.shopease.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Validated
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final StreamBridge streamBridge;
    private final PaymentClient paymentClient;

    /**
     * Order Creation Process
     * Extract All Product IDs in List
     * Fetch All Products from Product service
     * Map with
     */
    @Override
    @Transactional
    public OrderResponse createOrder(final OrderRequest request) {
        // Extract product IDs
        List<UUID> productIds = request.items()
                .stream()
                .map(OrderRequestItem::productId)
                .toList();
        // Fetch Products
        List<ProductResponse> products = productClient.getProductById(productIds);

        Map<UUID, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::productId, p -> p));

        // prepare inventory request
        Map<UUID, Integer> stockRequest = request.items().stream()
                .collect(Collectors.toMap(OrderRequestItem::productId, OrderRequestItem::quantity));

        // check inventory before creating order
        Map<UUID, Boolean> stockResult = inventoryClient.checkStock(stockRequest);

        for (Map.Entry<UUID, Boolean> entry : stockResult.entrySet()) {
            if (!entry.getValue()) {
                throw new RuntimeException("Stock not available for product " + entry.getKey());
            }
        }

        inventoryClient.reserveStock(stockRequest);
        // Create order
        try {
            Order order = Order.builder()
                    .userId(request.userId())
                    .orderStatus(OrderStatus.CREATED)
                    .build();

            BigDecimal grandTotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            // Build order items
            for (OrderRequestItem item : request.items()) {
                ProductResponse product = productMap.get(item.productId());

                if (product == null) {
                    throw new RuntimeException("Product not found: " + item.productId());
                }

                BigDecimal price = product.price();
                BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(item.quantity()));
                grandTotal = grandTotal.add(totalPrice);

                OrderItem orderItem = OrderItem.builder()
                        .productId(item.productId())
                        .name(product.name())
                        .quantity(item.quantity())
                        .price(product.price())
                        .order(order)
                        .build();
                orderItems.add(orderItem);
            }
            order.setItems(orderItems);
            order.setTotalPrice(grandTotal);
            orderRepository.save(order);


            List<OrderItemResponse> response = orderItems.stream()
                    .map(item -> {
                        ProductResponse product = productMap.get(item.getProductId());

                        return OrderItemResponse.builder()
                                .orderItemId(item.getOrderItemId())
                                .productId(item.getProductId())
                                .productName(product.name())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build();
                    }).toList();
            List<String> productName = orderItems.stream()
                    .map(item -> {
                        ProductResponse product = productMap.get(item.getProductId());
                        return product.name();
                    })
                    .toList();
            OrderDetails details = OrderDetails.builder()
                    .userId(order.getUserId())
                    .orderId(order.getOrderId())
                    .productName(productName)
                    .grandTotal(grandTotal)
                    .build();

            PaymentCreateDetails paymentCreateDetails = PaymentCreateDetails.builder()
                    .orderId(order.getOrderId())
                    .amount(grandTotal)
                    .userId(order.getUserId())
                    .build();

            PaymentResponse res = paymentClient.createPayment(paymentCreateDetails);

            if (res.paymentId() != null && res.orderId() != null) {
                orderRepository.findOrderByOrderId(res.orderId())
                        .ifPresentOrElse(o -> {
                            if (o.getOrderStatus() == OrderStatus.CREATED) {
                                System.out.println("Changed order status ");
                                o.setOrderStatus(OrderStatus.PAYMENT_PENDING);
                                orderRepository.save(order);
                            }
                        }, () -> {
                            throw new RuntimeException("Order Not Found Yet with Order Id: " + res.orderId());
                        });

            }

            return OrderResponse.builder()
                    .orderId(res.orderId())
                    .userId(order.getUserId())
                    .paymentId(res.paymentId())
                    .orderItemResponse(response)
                    .grandTotal(grandTotal)
                    .status(order.getOrderStatus())
                    .message("Order Created Successfully")
                    .build();
        } catch (Exception ex) {
            inventoryClient.releaseStock(stockRequest);
            throw ex;
        }
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrderAndProceedToBuy(final UUID userId, final ProductDetails details) {
        Order order = Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.CREATED)
                .build();

        BigDecimal price = details.price();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(details.quantity()));

        List<OrderItem> items = new ArrayList<>();

        OrderItem item = OrderItem.builder()
                .productId(details.productId())
                .name(details.name())
                .quantity(details.quantity())
                .price(price)
                .order(order)
                .build();

        items.add(item);

        order.setItems(items);
        order.setTotalPrice(totalPrice);

        orderRepository.save(order);
        List<String> name = List.of(item.getName());

        PaymentCreateDetails paymentCreateDetails = PaymentCreateDetails.builder()
                .orderId(order.getOrderId())
                .amount(totalPrice)
                .userId(order.getUserId())
                .build();


        return CreateOrderResponse.builder()
                .orderId(item.getOrder().getOrderId())
                .message("Order Created Successfully and Proceed to buy")
                .build();
    }

    @Override
    @Transactional
    public void updateOrderStatus(CreateOrderResponseToUpdateStatus request) {

        orderRepository.findOrderByOrderId(request.orderId())
                .ifPresentOrElse(order -> {
                    if (order.getOrderStatus() == OrderStatus.PAYMENT_PENDING) {
                        order.setOrderStatus(OrderStatus.CONFIRMED);
                        orderRepository.save(order);
                    }
                }, () -> {
                    throw new RuntimeException("Order Not Found Yet with Order Id: " + request.orderId());
                });

        notifyUserAboutOrder(request);

    }

    @Transactional(readOnly = true)
    public void notifyUserAboutOrder(CreateOrderResponseToUpdateStatus response) {
        Order order = orderRepository
                .findOrderByOrderIdWithItems(response.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<String> productNames = order.getItems()
                .stream()
                .map(OrderItem::getName)
                .toList();

        OrderDetails orderDetails = OrderDetails.builder()
                .userId(response.userId())
                .orderId(response.orderId())
                .paymentId(response.paymentId())
                .productName(productNames)
                .grandTotal(response.grandTotal())
                .build();

        notifyToUserOrderCreated(orderDetails);
    }


    private void notifyToUserOrderCreated(final OrderDetails details) {
        streamBridge.send("orderNotificationEvent-out-0", details);
    }
}
