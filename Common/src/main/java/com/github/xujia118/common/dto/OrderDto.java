package com.github.xujia118.common.dto;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.common.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String orderId;     // Flattened from OrderKey
    private Long accountId;   // Flattened from OrderKey
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long paymentMethodId;
    private PaymentType paymentType;
}