package com.github.xujia118.paymentservice.service;

import com.github.xujia118.paymentservice.model.Payment;

public interface PaymentProvider {
    boolean executeTransaction(Payment payment, Long PaymentMethodId);
}
