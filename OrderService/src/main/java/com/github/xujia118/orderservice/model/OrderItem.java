package com.github.xujia118.orderservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Data
@UserDefinedType("order_item")
public class OrderItem {
    private String itemId; // From ItemService (MongoDB ID)
    private String itemName;
    private int quantity;
    private double priceAtPurchase; // Snapshot the price for historical accuracy
}
