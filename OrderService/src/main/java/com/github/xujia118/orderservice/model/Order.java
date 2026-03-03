package com.github.xujia118.orderservice.model;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.common.model.PaymentType;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.util.List;

@Table("orders")
@Data
public class Order {

    @PrimaryKey
    private OrderKey key; // Contains accountId (Partition) and orderId (Clustering)

    @Column("items")
    private List<OrderItem> items;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("order_status")
    private OrderStatus status;

    @Column("payment_method_id")
    private Long paymentMethodId;

    @Column("payment_type")
    private PaymentType paymentType;

    public void calculateTotal() {
        if (this.items == null || this.items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }

        this.totalAmount = this.items.stream()
                .map(item -> {
                    // Ensure item.getPriceAtPurchase() also returns BigDecimal
                    BigDecimal price = BigDecimal.valueOf(item.getPriceAtPurchase());
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                    return price.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
