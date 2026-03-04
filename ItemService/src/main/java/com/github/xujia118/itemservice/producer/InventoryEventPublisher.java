package com.github.xujia118.itemservice.producer;

import com.github.xujia118.common.constant.KafkaTopics;
import com.github.xujia118.common.dto.InventoryFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishFailure(String orderId, String accountId, String transactionId, BigDecimal totalAmount) {
        InventoryFailedEvent event = new InventoryFailedEvent(orderId, accountId, transactionId, totalAmount);

        log.info("Publishing InventoryFailedEvent for Order: {}", orderId);

        // We use orderId as the message key to ensure all events for the same order
        // land in the same Kafka partition (maintaining order if necessary)
        kafkaTemplate.send(KafkaTopics.INVENTORY_FAILURE_TOPIC.getTopicName(), orderId, event);
    }
}