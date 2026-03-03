package com.github.xujia118.paymentservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class OrderKey {
    private String accountId;
    private String orderId;
}
