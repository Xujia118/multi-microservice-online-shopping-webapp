package com.github.xujia118.paymentservice.service;

import com.github.xujia118.paymentservice.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class MockPaymentProvider implements PaymentProvider {

    private final Random random = new Random();

    @Override
    public boolean executeTransaction(Payment payment, Long PaymentMethodId) {
        log.info("Mocking third-party call for Order: {}", payment.getOrderId());

        // Simulate external API latency
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 90% Success Rate logic
        boolean isSuccess = random.nextDouble() < 0.9;

        if (isSuccess) {
            String mockTxId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            payment.setTransactionId(mockTxId); // Update the entity with the bank's reference
            log.info("Mock Provider: Payment authorized. ID: {}", mockTxId);
        } else {
            log.error("Mock Provider: Payment declined by bank.");
        }

        return isSuccess;
    }

    @Override
    public void refund(String transactionId, BigDecimal totalAmount) {
        log.info("Mocking refund for Transaction: {} | Amount: ${}", transactionId, totalAmount);

        if (random.nextDouble() < 0.05) {
            log.error("Bank API timeout for refund of ${}", totalAmount);
            throw new RuntimeException("Refund failed");
        }

        log.info("Mock Provider: Refund of ${} successful!", totalAmount);
    }
}
