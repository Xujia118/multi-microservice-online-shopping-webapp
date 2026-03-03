package com.github.xujia118.paymentservice.consumer;

import com.github.xujia118.paymentservice.model.Order;
import com.github.xujia118.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    // The containerFactory name must match the @Bean name in your KafkaConfig
    @KafkaListener(
            topics = "order-topic",
            groupId = "payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderReceived(Order order) {
        log.info("Payment Service received Order ID: {}", order.getKey());
        log.info("Amount to charge: ${}", order.getTotalAmount());
        log.info("Using Payment Method: {}", order.getPaymentMethodId());

        boolean result = paymentService.process(order);

        if (result) {
            // TODO: Future step - Publish a 'payment-success' event back to Kafka
        } else {
            // TODO: Future step - Publish a 'payment-failed' event to trigger a refund or cancellation
        }
    }
}
