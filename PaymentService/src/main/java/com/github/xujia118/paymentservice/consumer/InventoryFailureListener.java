package com.github.xujia118.paymentservice.consumer;

import com.github.xujia118.common.dto.InventoryFailedEvent;
import com.github.xujia118.paymentservice.repository.PaymentRepository;
import com.github.xujia118.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryFailureListener {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @KafkaListener(
            topics = "${kafka.topics.inventory-failure}",
            groupId = "${kafka.groups.payment-service-refunds}",
            properties = {
                    "spring.json.value.default.type=com.github.xujia118.common.dto.InventoryFailedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    public void handleInventoryFailure(InventoryFailedEvent event) {
        log.info("Compensating payment for Order: {}", event.getOrderId());

        try {
            // Pass both IDs to the service
            paymentService.processRefund(event);
            log.info("Refund cycle complete for Order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Refund failed for order {}: {}", event.getOrderId(), e.getMessage());
            // Do NOT catch and swallow the exception if you want Kafka to retry.
            // Throwing it here lets the Kafka ErrorHandler take over.
            throw e;
        }
    }
}
