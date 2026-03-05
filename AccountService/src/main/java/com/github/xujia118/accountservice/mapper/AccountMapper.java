package com.github.xujia118.accountservice.mapper;

import com.github.xujia118.accountservice.dto.AccountDto;
import com.github.xujia118.accountservice.dto.AddressDto;
import com.github.xujia118.accountservice.dto.PaymentMethodDto;
import com.github.xujia118.accountservice.model.Account;
import com.github.xujia118.accountservice.model.Address;
import com.github.xujia118.accountservice.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AccountMapper {
    public Account toEntity(AccountDto dto) {
        if (dto == null) return null;

        Account account = new Account();
        account.setId(dto.getId());
        account.setEmail(dto.getEmail());
        account.setFirstName(dto.getFirstName());
        account.setLastName(dto.getLastName());

        // Map Addresses
        if (dto.getAddresses() != null) {
            account.setAddresses(dto.getAddresses().stream().map(addressDto -> {
                Address address = new Address();
                address.setStreet(addressDto.getStreet());
                address.setCity(addressDto.getCity());
                address.setState(addressDto.getState());
                address.setZipCode(addressDto.getZipCode());
                address.setDefault(addressDto.isDefault());
                address.setAccount(account); // Establish Bi-directional link
                return address;
            }).collect(Collectors.toList()));
        }

        // Map Payment Methods
        if (dto.getPaymentMethods() != null) {
            account.setPaymentMethods(dto.getPaymentMethods().stream().map(paymentDto -> {
                PaymentMethod pm = new PaymentMethod();
                pm.setType(paymentDto.getType());
                pm.setCardNumber(paymentDto.getCardNumber());
                pm.setExpirationDate(paymentDto.getExpirationDate());
                pm.setCardHolderName(paymentDto.getCardHolderName());
                pm.setAccount(account); // Establish Bi-directional link
                return pm;
            }).collect(Collectors.toList()));
        }

        return account;
    }

    public AccountDto toDto(Account entity) {
        if (entity == null) return null;

        AccountDto dto = new AccountDto();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        // Do NOT map password back to DTO for security reasons
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());

        // Map Addresses
        if (entity.getAddresses() != null) {
            dto.setAddresses(entity.getAddresses().stream()
                    .map(this::mapAddressToDto) // Call helper method
                    .collect(Collectors.toList()));
        }

        // Map Payment Methods
        if (entity.getPaymentMethods() != null) {
            dto.setPaymentMethods(entity.getPaymentMethods().stream()
                    .map(this::mapPaymentToDto) // Call helper method
                    .collect(Collectors.toList()));
        }

        // Mapping lists back to DTOs follows the same stream pattern...
        return dto;
    }

    // Helper for Address
    private AddressDto mapAddressToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setDefault(address.isDefault());
        return dto;
    }

    // Helper for Payment
    private PaymentMethodDto mapPaymentToDto(PaymentMethod pm) {
        PaymentMethodDto dto = new PaymentMethodDto();
        dto.setId(pm.getId());
        dto.setType(pm.getType());
        dto.setCardNumber(pm.getCardNumber()); // Usually you'd mask this here!
        dto.setExpirationDate(pm.getExpirationDate());
        dto.setCardHolderName(pm.getCardHolderName());
        return dto;
    }
}
