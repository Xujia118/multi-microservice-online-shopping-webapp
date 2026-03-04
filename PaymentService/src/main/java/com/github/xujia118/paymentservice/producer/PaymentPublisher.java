package com.github.xujia118.paymentservice.producer;

import com.github.xujia118.common.constant.KafkaTopics;
import com.github.xujia118.common.dto.OrderDto;
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

    public void publishPaymentSuccess(OrderDto orderDto, Payment payment) {
        publish(orderDto, payment, KafkaTopics.PAYMENT_SUCCESS_TOPIC);
    }

    public void publishPaymentFailure(OrderDto orderDto, Payment payment) {
        publish(orderDto, payment, KafkaTopics.PAYMENT_FAILURE_TOPIC);
    }

    // Generic internal method to handle the heavy lifting
    private void publish(OrderDto orderDto, Payment payment, KafkaTopics topic) {
        PaymentDto paymentDto = mapToDto(orderDto, payment);

        log.info("Publishing payment status [{}] to topic [{}] for order: {}",
                paymentDto.getStatus(), topic.getTopicName(), paymentDto.getOrderId());

        kafkaTemplate.send(topic.getTopicName(), paymentDto.getOrderId(), paymentDto);
    }

    // Dedicated mapper method
    private PaymentDto mapToDto(OrderDto orderDto, Payment payment) {
        return new PaymentDto(
                payment.getOrderId(),
                payment.getAccountId(),
                payment.getTransactionId(),
                payment.getOrderStatus().toString(),
                orderDto.getItems()
        );
    }
}
