package com.github.xujia118.paymentservice.producer;

import com.github.xujia118.common.constant.KafkaTopics;
import com.github.xujia118.common.dto.PaymentDto;
import com.github.xujia118.paymentservice.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSuccess(Payment payment) {
        PaymentDto paymentDto = new PaymentDto(
                payment.getOrderId(),
                payment.getAccountId(),
                payment.getTransactionId(),
                payment.getOrderStatus().toString()
        );

        log.info("Publishing payment success for order: {}", paymentDto.getOrderId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_SUCCESS_TOPIC.getTopicName(), paymentDto.getOrderId(), paymentDto);
    }

    public void publishPaymentFailure(Payment payment) {
        PaymentDto paymentDto = new PaymentDto(
                payment.getOrderId(),
                payment.getAccountId(),
                payment.getTransactionId(),
                payment.getOrderStatus().toString()
        );

        log.info("Publishing payment failed for order: {}", paymentDto.getOrderId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILURE_TOPIC.getTopicName(), paymentDto.getOrderId(), paymentDto);
    }
}
