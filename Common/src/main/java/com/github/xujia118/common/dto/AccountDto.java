package com.github.xujia118.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private long id;

    private String email;

    private String firstName;
    private String lastName;

    private List<AddressDto> addresses;
    private List<PaymentMethodDto> paymentMethods;
}