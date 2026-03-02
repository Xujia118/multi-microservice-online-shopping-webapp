package com.github.xujia118.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.xujia118.common.model.PaymentType;
import lombok.Data;
import java.util.List;

@Data
public class AccountDto {
    private long id;

    private String email;

    @JsonProperty(access =  JsonProperty.Access.WRITE_ONLY)
    private String password; // Only used during Creation/Login

    private String firstName;
    private String lastName;

    private List<AddressDto> addresses;
    private List<PaymentMethodDto> paymentMethods;
}