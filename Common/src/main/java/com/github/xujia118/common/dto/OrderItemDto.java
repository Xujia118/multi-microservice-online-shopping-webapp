package com.github.xujia118.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private String itemId;
    private String itemName;
    private Integer quantity;
    private Double priceAtPurchase;
}