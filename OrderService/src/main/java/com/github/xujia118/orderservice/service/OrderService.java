package com.github.xujia118.orderservice.service;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderKey;
import com.github.xujia118.orderservice.producer.OrderPublisher;
import com.github.xujia118.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderPublisher orderPublisher;

    public List<Order> getOrdersByAccount(Long accountId) {
        return orderRepository.findByKeyAccountId(accountId);
    }

    public Order getOrderById(Long accountId, UUID orderId) {
        // 1. Get the composite key
        OrderKey key = getOrderKey(accountId, orderId);

        // 2. Fetch from repository and handle the "Not Found" case securely
        return orderRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));
    }

    public Order createOrder(Long accountId, Order order) {
        // 1. Make a new composite key and set it
        OrderKey key = getOrderKey(accountId, UUID.randomUUID());
        order.setKey(key);

        // 2. Set the initial lifecycle status
        order.setStatus(OrderStatus.PENDING);

        // 3. Calculate total amount
        // you shouldn't trust json data from the frontend
        // you should call item service and calculate total amount using the price there
        // but skipped for this project

        order.calculateTotal();

        // 4. Persist to Cassandra
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved to Cassandra: {}", savedOrder.getKey().getOrderId());

        // 5. Publish to Kafka
        orderPublisher.publishOrder(savedOrder);
        return savedOrder;
    }

    public Order updateOrder(Long accountId, UUID orderId, Order updatedOrder) {
        Order order = getOrderById(accountId, orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order can only be updated while in PENDING status."
            );
        }

        order.setItems(updatedOrder.getItems());
        order.setPaymentType(updatedOrder.getPaymentType());
        order.setPaymentMethodId(updatedOrder.getPaymentMethodId());
        order.calculateTotal();

        Order savedOrder = orderRepository.save(order);
        orderPublisher.publishOrder(savedOrder);

        return savedOrder;
    }

    public Order cancelOrder(Long accountId, UUID orderId) {
        Order order = getOrderById(accountId, orderId);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot cancel order once it is shipped or delivered."
            );
        }

        // 3. Process cancellation
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        orderPublisher.publishOrder(savedOrder);
        return savedOrder;
    }

    private OrderKey getOrderKey(Long accountId, UUID orderId) {
        OrderKey key = new OrderKey();
        key.setAccountId(accountId);
        key.setOrderId(orderId);
        return key;
    }
}
