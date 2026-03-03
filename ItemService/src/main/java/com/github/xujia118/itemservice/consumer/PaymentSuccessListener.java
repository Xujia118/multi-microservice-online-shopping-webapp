package com.github.xujia118.itemservice.consumer;

import com.github.xujia118.common.dto.PaymentDto;
import com.github.xujia118.itemservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentSuccessListener {

    private final ItemService itemService;

    @KafkaListener(topics = "payment-success-topic", groupId = "inventory-group")
    public void onPaymentSuccess(PaymentDto event) {
        log.info("Received payment success for Order ID: {}", event.getOrderId());

        // Ensure we only process if the status is PAID
        if ("PAID".equals(event.getStatus())) {
            itemService.deductStock(event.getOrderId());
        }
    }
}
