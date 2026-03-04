package com.github.xujia118.paymentservice.service;

import com.github.xujia118.paymentservice.model.Payment;

import java.math.BigDecimal;

public interface PaymentProvider {
    boolean executeTransaction(Payment payment, Long PaymentMethodId);

    void refund(String transactionId, BigDecimal totalAmount);
}
