package com.github.xujia118.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopics {
    ORDER_TOPIC("order-topic"),
    PAYMENT_SUCCESS_TOPIC("payment-success-topic"),
    PAYMENT_FAILURE_TOPIC("payment-failure-topic"),
    INVENTORY_DEDUCTED_TOPIC("inventory-deducted-topic");

    private final String topicName;
}