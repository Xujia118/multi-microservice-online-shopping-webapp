package com.github.xujia118.accountservice.model;

import com.github.xujia118.common.model.PaymentType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payment_methods")
@Data
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentType type; // e.g., CREDIT_CARD, PAYPAL

    private String cardNumber; // Encrypted or masked
    private String expirationDate;
    private String cardHolderName;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}