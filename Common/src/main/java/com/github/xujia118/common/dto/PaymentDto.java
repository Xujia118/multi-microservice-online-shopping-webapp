package com.github.xujia118.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String orderId;     // Extracted from OrderKey
    private String accountId;   // Extracted from OrderKey
    private String transactionId;
    private String status;
}
