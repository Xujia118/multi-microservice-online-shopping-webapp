package com.github.xujia118.itemservice.consumer;

import com.github.xujia118.common.dto.PaymentDto;
import com.github.xujia118.common.model.OrderStatus;
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

    @KafkaListener(topics = "${kafka.topics.payment-success}", groupId = "${kafka.groups.inventory}")
    public void onPaymentSuccess(PaymentDto paymentDto) {
        log.info("Received payment success for Order ID: {}", paymentDto.getOrderId());

        // Ensure we only process if the status is PAID
        if (OrderStatus.PAID.equals(paymentDto.getOrderStatus())) {
            itemService.deductStock(paymentDto);
        }
    }
}
