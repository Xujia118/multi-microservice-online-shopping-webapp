package com.github.xujia118.common.model;

public enum OrderStatus {
    PENDING,    // Order created, waiting for payment
    PAID,       // Payment successful
    SHIPPED,    // Out for delivery
    DELIVERED,  // Customer received it
    CANCELLED,  // User cancelled before payment/shipping
    REFUNDED,    // Payment reversed (matches your Payment Service requirement)
    DELETED,     // Order deleted by user, but kept by platform for tax & audit purpose
    FAILED
}
