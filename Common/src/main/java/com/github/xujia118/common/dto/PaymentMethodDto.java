package com.github.xujia118.common.dto;

import com.github.xujia118.common.model.PaymentType;
import lombok.Data;

@Data
public class PaymentMethodDto {
    private Long id;
    private PaymentType type;
    private String cardNumber;
    private String expirationDate;
    private String cardHolderName;
}
