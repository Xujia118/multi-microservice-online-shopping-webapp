package com.github.xujia118.paymentservice.service;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.paymentservice.model.Order;
import com.github.xujia118.paymentservice.model.Payment;
import com.github.xujia118.paymentservice.producer.PaymentPublisher;
import com.github.xujia118.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentPublisher paymentPublisher;

    @Transactional
    public void processOrder(Order order) {
        // 1. Idempotency Check (Don't process the same order twice)
        if (paymentRepository.findByOrderKey(order.getKey()).isPresent()) {
            log.warn("Payment already exists for Order: {}", order.getKey().getOrderId());
            return;
        }

        // 2. Map DTO to Entity & Save Initial PENDING state
        Payment payment = createInitialPayment(order);
        payment = paymentRepository.save(payment);

        // 3. Execute Transaction (Mock Provider)
        boolean success = paymentProvider.executeTransaction(payment, order.getPaymentMethodId());

        // 4. Update and Publish
        if (success) {
            handleSuccess(payment);
        } else {
            handleFailure(payment);
        }
    }

    private Payment createInitialPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrderKey(order.getKey());
        payment.setAmount(BigDecimal.valueOf(order.getTotalAmount()));
        payment.setPaymentType(order.getPaymentType());
        payment.setOrderStatus(OrderStatus.PENDING);
        return payment;
    }

    private void handleSuccess(Payment payment) {
        payment.setOrderStatus(OrderStatus.PAID);
        paymentRepository.save(payment);
        log.info("Payment SUCCESS saved for Order: {}", payment.getOrderKey().getOrderId());

        // Notify downstream services via Kafka
        paymentPublisher.publishPaymentSuccess(payment);
    }

    private void handleFailure(Payment payment) {
        payment.setOrderStatus(OrderStatus.FAILED);
        paymentRepository.save(payment);
        log.error("Payment FAILED saved for Order: {}", payment.getOrderKey().getOrderId());

        // Notify downstream services via Kafka
        paymentPublisher.publishPaymentFailure(payment);
    }
}
