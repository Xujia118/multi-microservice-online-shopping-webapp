package com.github.xujia118.paymentservice.service;

import com.github.xujia118.paymentservice.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class PaymentService {

    private final Random random = new Random();

    public boolean process(Order order) {
        log.info("Initiating payment for Order: {} via {}",
                order.getKey().getOrderId(),
                order.getPaymentType());

        // Simulate network latency to a bank API
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock a 90% success rate
        boolean isSuccess = random.nextDouble() < 0.9;

        if (isSuccess) {
            log.info("Payment SUCCESSFUL for Order: {}", order.getKey().getOrderId());
        } else {
            log.error("Payment FAILED for Order: {}", order.getKey().getOrderId());
        }

        return isSuccess;
    }
}
