package com.github.xujia118.paymentservice.service;

import com.github.xujia118.common.dto.InventoryFailedEvent;
import com.github.xujia118.common.dto.OrderDto;
import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.paymentservice.model.Payment;
import com.github.xujia118.paymentservice.producer.PaymentPublisher;
import com.github.xujia118.paymentservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentPublisher paymentPublisher;

    @Transactional
    public void processOrder(OrderDto orderDto) {
        // 1. Idempotency Check (Don't process the same order twice)
        if (paymentRepository.findByOrderId(orderDto.getOrderId()).isPresent()) {
            log.warn("Payment already exists for Order: {}", orderDto.getOrderId());
            return;
        }

        // 2. Map DTO to Entity & Save Initial PENDING state
        Payment payment = createInitialPayment(orderDto);
        payment = paymentRepository.save(payment);

        // 3. Execute Transaction (Mock Provider)
        boolean success = paymentProvider.executeTransaction(payment, orderDto.getPaymentMethodId());

        // 4. Update and Publish
        if (success) {
            handleSuccess(orderDto, payment);
        } else {
            handleFailure(orderDto, payment);
        }
    }

    private Payment createInitialPayment(OrderDto orderDto) {
        Payment payment = new Payment();
        payment.setOrderId(orderDto.getOrderId());
        payment.setAccountId(orderDto.getAccountId());
        payment.setTotalAmount(orderDto.getTotalAmount());
        payment.setPaymentType(orderDto.getPaymentType());
        payment.setOrderStatus(OrderStatus.PENDING);
        return payment;
    }

    private void handleSuccess(OrderDto orderDto, Payment payment) {
        payment.setOrderStatus(OrderStatus.PAID);
        paymentRepository.save(payment);
        log.info("Payment SUCCESS saved for Order: {}", payment.getOrderId());

        // Notify downstream services via Kafka
        paymentPublisher.publishPaymentSuccess(orderDto, payment);
    }

    private void handleFailure(OrderDto orderDto, Payment payment) {
        payment.setOrderStatus(OrderStatus.FAILED);
        paymentRepository.save(payment);
        log.error("Payment FAILED saved for Order: {}", payment.getOrderId());

        // Notify downstream services via Kafka
        paymentPublisher.publishPaymentFailure(orderDto, payment);
    }

    @Transactional
    public void processRefund(InventoryFailedEvent event) {
        // 1. Fetch the payment record
        Payment payment = paymentRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + event.getOrderId()));

        // 2. The Idempotency Guard
        if (OrderStatus.REFUNDED.equals(payment.getOrderStatus())) {
            log.info("Payment for order {} already refunded. Skipping.", event.getOrderId());
            return;
        }

        // 3. Trigger external refund
        // Note: Call this before the DB update. If the API fails, the DB doesn't change.
        paymentProvider.refund(event.getTransactionId(), payment.getTotalAmount());

        // 4. Update the record
        payment.setOrderStatus(OrderStatus.REFUNDED);
        paymentRepository.save(payment);
    }
}
