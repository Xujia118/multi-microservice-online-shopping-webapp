package com.github.xujia118.orderservice.producer;

import com.github.xujia118.common.constant.KafkaTopics;
import com.github.xujia118.common.dto.OrderDto;
import com.github.xujia118.common.dto.OrderItemDto;
import com.github.xujia118.orderservice.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPublisher {

    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    public void publishOrder(Order order) {
        OrderDto dto = OrderDto.builder()
                .orderId(order.getKey().getOrderId().toString())
                .accountId(order.getKey().getAccountId().toString())
                .items(order.getItems().stream()
                        .map(item -> new OrderItemDto(
                                item.getItemId(),
                                item.getItemName(),
                                item.getQuantity(),
                                item.getPriceAtPurchase()
                        )).collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethodId(order.getPaymentMethodId())
                .paymentType(order.getPaymentType())
                .build();

        // Using orderId as key for even distribution across partitions
        kafkaTemplate.send(KafkaTopics.ORDER_TOPIC.getTopicName(), dto.getOrderId(), dto);
        log.info("Order event published for ID: {}", dto.getOrderId());
    }
}