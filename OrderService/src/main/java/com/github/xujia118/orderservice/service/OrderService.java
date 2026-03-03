package com.github.xujia118.orderservice.service;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderKey;
import com.github.xujia118.orderservice.producer.OrderPublisher;
import com.github.xujia118.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final KafkaTemplate<String, Order> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final OrderPublisher orderPublisher;

    public List<Order> getOrdersByAccount(Long accountId) {
        return orderRepository.findByKeyAccountId(accountId);
    }

    public Order getOrderById(OrderKey orderKey) {
        return orderRepository.findById(orderKey)
                .orElseThrow(() -> new RuntimeException("Order not found!"));
    }

    public Order createOrder(Order order) {
        // 1. Generate the Clustering Key (orderId) if it's not already provided
        if (order.getKey().getOrderId() == null) {
            order.getKey().setOrderId(UUID.randomUUID());
        }

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

        // consider race conditions
        return savedOrder;
    }

    public Order updateOrder(OrderKey orderKey, Order updatedOrder) {
        Order order = getOrderById(orderKey);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be updated while in PENDING status.");
        }

        order.setItems(updatedOrder.getItems());
        order.setPaymentType(updatedOrder.getPaymentType());
        order.setPaymentMethodId(updatedOrder.getPaymentMethodId());
        order.calculateTotal();

        Order savedOrder = orderRepository.save(order);
        orderPublisher.publishOrder(savedOrder);

        return savedOrder;
    }

    public Order cancelOrder(OrderKey orderKey) {
        if (!orderRepository.existsById(orderKey)) {
            throw new RuntimeException("Order not found!");
        }

        Order order = getOrderById(orderKey);

        // Logical check: Don't cancel if already shipped or delivered
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order once it's shipped.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        orderPublisher.publishOrder(savedOrder);
        return order;
    }
}
