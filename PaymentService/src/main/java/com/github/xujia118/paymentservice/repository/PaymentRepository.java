package com.github.xujia118.paymentservice.repository;

import com.github.xujia118.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 1. Find specifically by the orderId inside the orderKey
    // This matches the "orderKey" field name + the "orderId" field inside OrderKey
    Optional<Payment> findByOrderId(String orderId);

    // 2. Find by accountId inside the orderKey
    List<Payment> findByAccountId(Long accountId);
}
