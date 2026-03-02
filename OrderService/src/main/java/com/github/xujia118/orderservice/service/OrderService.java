package com.github.xujia118.orderservice.service;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderKey;
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
        // We use the accountId as the Kafka partition key to keep user orders in order
        try {
            String kafkaKey = savedOrder.getKey().getAccountId().toString();
            kafkaTemplate.send("order-topic", kafkaKey, savedOrder);
        } catch (Exception e) {
            // In a real system, you'd handle Kafka failures (e.g., Transactional Outbox)
            log.error("Failed to publish order to Kafka", e);
        }

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

        kafkaTemplate.send("order-topic", orderKey.getAccountId().toString(), savedOrder);
        log.info("Order {} updated. New total: {}", orderKey.getOrderId(), savedOrder.getTotalAmount());

        return savedOrder;
    }

    public Order cancelOrder(OrderKey orderKey) {
        if (!orderRepository.existsById(orderKey)) {
            throw new RuntimeException("Order not found!");
        }

        Order order = getOrderById(orderKey);

        // Logical check: Don't cancel if already shipped or delivered
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order once it's shipped or delivered.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Kafka event triggers downstream Refund & Inventory Restock
        kafkaTemplate.send("order-topic", orderKey.getAccountId().toString(), order);

        log.info("Order {} cancelled", orderKey.getOrderId());

        return order;
    }
}
