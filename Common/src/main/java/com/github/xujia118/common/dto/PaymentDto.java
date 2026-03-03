package com.github.xujia118.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String orderId;     // Extracted from OrderKey
    private String accountId;   // Extracted from OrderKey
    private String transactionId;
    private String status;
    private List<OrderItemDto> items;
}
