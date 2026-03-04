package com.github.xujia118.paymentservice.consumer;

import com.github.xujia118.common.dto.OrderDto;
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

    @KafkaListener(topics = "${kafka.topics.order}", groupId = "${kafka.groups.payment-service-new-orders}")
    public void onOrderReceived(OrderDto orderDto) {
        log.info("Payment Service received Order ID: {}", orderDto.getOrderId());
        log.info("Amount to charge: ${}", orderDto.getTotalAmount());
        log.info("Using Payment Method: {}", orderDto.getPaymentMethodId());

        paymentService.processOrder(orderDto);
    }
}
