package com.github.xujia118.paymentservice.model;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.common.model.PaymentType;
import lombok.Data;

@Data
public class Order {
    // 1. Identification (For database updates and tracking)
    private OrderKey key;

    // 2. Financial (The most critical part)
    private double totalAmount;

    // 3. Payment Context (Needed by the Gateway)
    private PaymentType paymentType;
    private String paymentMethodId; // Tokenized ID from the frontend (e.g., "pm_123...")

    // 4. Status (For the Saga pattern)
    private OrderStatus status;
}
