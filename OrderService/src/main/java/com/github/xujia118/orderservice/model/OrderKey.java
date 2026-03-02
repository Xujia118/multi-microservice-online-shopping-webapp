package com.github.xujia118.orderservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

@PrimaryKeyClass
@Data
public class OrderKey {
    @PrimaryKeyColumn(name = "account_id", type = PrimaryKeyType.PARTITIONED)
    private Long accountId;

    @PrimaryKeyColumn(name = "order_id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    private UUID orderId;
}
