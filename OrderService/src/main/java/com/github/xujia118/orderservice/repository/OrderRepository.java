package com.github.xujia118.orderservice.repository;

import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CassandraRepository<Order, OrderKey> {
    // Return all orders of a user
    List<Order> findByKeyAccountId(Long accountId);

    // Return a single order - it's given for free!
    // findById(OrderKey);
}

