package com.github.xujia118.accountservice.service;

import com.github.xujia118.accountservice.dto.AccountDto;
import com.github.xujia118.accountservice.dto.AddressDto;
import com.github.xujia118.accountservice.dto.PaymentMethodDto;
import com.github.xujia118.accountservice.mapper.AccountMapper;
import com.github.xujia118.accountservice.model.Account;
import com.github.xujia118.accountservice.model.Address;
import com.github.xujia118.accountservice.model.PaymentMethod;
import com.github.xujia118.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public Account getAccount(long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Transactional
    public Account createAccount(AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(long id, AccountDto accountDto) {
        Account existingAccount = getAccount(id);

        // Update core fields
        if (accountDto.getFirstName() != null) {
            existingAccount.setFirstName(accountDto.getFirstName());
        }

        if (accountDto.getLastName() != null) {
            existingAccount.setLastName(accountDto.getLastName());
        }

        if (accountDto.getEmail() != null) {
            existingAccount.setEmail(accountDto.getEmail());
        }

        // We don't update password here

        updateAddresses(existingAccount, accountDto);
        updatePaymentMethods(existingAccount, accountDto);

        return accountRepository.save(existingAccount);
    }

    private void updateAddresses(Account existingAccount, AccountDto dto) {
        if (dto.getAddresses() == null) return;

        // 1. Identify which addresses to remove (Present in DB but NOT in Request)
        Set<Long> incomingIds = dto.getAddresses().stream()
                .map(AddressDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingAccount.getAddresses().removeIf(address -> !incomingIds.contains(address.getId()));

        // 2. Process incoming addresses
        for (AddressDto addressDto : dto.getAddresses()) {
            if (addressDto.getId() != null) {
                // SCENARIO: Update Existing
                existingAccount.getAddresses().stream()
                        .filter(a -> a.getId().equals(addressDto.getId()))
                        .findFirst()
                        .ifPresent(existingAddr -> {
                            existingAddr.setStreet(addressDto.getStreet());
                            existingAddr.setCity(addressDto.getCity());
                            existingAddr.setState(addressDto.getState());
                            existingAddr.setZipCode(addressDto.getZipCode());
                            existingAddr.setCountry(addressDto.getCountry());
                            existingAddr.setDefault(addressDto.isDefault());
                        });
            } else {
                // SCENARIO: Create New
                Address newAddr = new Address();
                newAddr.setStreet(addressDto.getStreet());
                newAddr.setCity(addressDto.getCity());
                newAddr.setState(addressDto.getState());
                newAddr.setZipCode(addressDto.getZipCode());
                newAddr.setCountry(addressDto.getCountry());
                newAddr.setDefault(addressDto.isDefault());

                newAddr.setAccount(existingAccount);
                existingAccount.getAddresses().add(newAddr);
            }
        }
    }

    private void updatePaymentMethods(Account existingAccount, AccountDto dto) {
        if (dto.getPaymentMethods() == null) return;

        // 1. Identify which IDs are coming in from the request
        Set<Long> incomingIds = dto.getPaymentMethods().stream()
                .map(PaymentMethodDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. DELETE: Remove any payment methods in the DB that aren't in the request
        // Requires 'orphanRemoval = true' on the @OneToMany mapping in the Account entity
        existingAccount.getPaymentMethods().removeIf(pm -> !incomingIds.contains(pm.getId()));

        // 3. UPDATE or CREATE
        for (PaymentMethodDto pmDto : dto.getPaymentMethods()) {
            if (pmDto.getId() != null) {
                // SCENARIO: Update Existing row
                existingAccount.getPaymentMethods().stream()
                        .filter(pm -> pm.getId().equals(pmDto.getId()))
                        .findFirst()
                        .ifPresent(existingPm -> {
                            existingPm.setType(pmDto.getType());
                            existingPm.setCardNumber(pmDto.getCardNumber());
                            existingPm.setExpirationDate(pmDto.getExpirationDate());
                            existingPm.setCardHolderName(pmDto.getCardHolderName());
                        });
            } else {
                // SCENARIO: Add New row
                PaymentMethod newPm = new PaymentMethod();
                newPm.setType(pmDto.getType());
                newPm.setCardNumber(pmDto.getCardNumber());
                newPm.setExpirationDate(pmDto.getExpirationDate());
                newPm.setCardHolderName(pmDto.getCardHolderName());
                newPm.setAccount(existingAccount); // Link back to parent
                existingAccount.getPaymentMethods().add(newPm);
            }
        }
    }

    @Transactional
    public void deleteAccount(long id) {
        Account account = getAccount(id);
        accountRepository.delete(account);
    }
}
