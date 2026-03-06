package com.github.xujia118.accountservice.controller;

import com.github.xujia118.common.dto.AccountDto;
import com.github.xujia118.accountservice.mapper.AccountMapper;
import com.github.xujia118.accountservice.model.Account;
import com.github.xujia118.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable long id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        Account account = accountService.createAccount(accountDto);
        return new ResponseEntity<>(accountMapper.toDto(account), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @RequestBody AccountDto accountDTO) {
        Account updatedAccount = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(accountMapper.toDto(updatedAccount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
