package com.github.xujia118.accountservice.service;

import com.github.xujia118.accountservice.mapper.AccountMapper;
import com.github.xujia118.accountservice.model.Account;
import com.github.xujia118.accountservice.model.Address;
import com.github.xujia118.accountservice.model.PaymentMethod;
import com.github.xujia118.accountservice.repository.AccountRepository;
import com.github.xujia118.common.dto.AccountDto;
import com.github.xujia118.common.dto.AddressDto;
import com.github.xujia118.common.dto.PaymentMethodDto;
import com.github.xujia118.common.model.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountDto testAccountDto;
    private Address testAddress;
    private AddressDto testAddressDto;
    private PaymentMethod testPaymentMethod;
    private PaymentMethodDto testPaymentMethodDto;

    @BeforeEach
    void setUp() {
        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setStreet("123 Main St");
        testAddress.setCity("New York");
        testAddress.setState("NY");
        testAddress.setZipCode("10001");
        testAddress.setCountry("USA");
        testAddress.setDefault(true);

        testAddressDto = new AddressDto();
        testAddressDto.setId(1L);
        testAddressDto.setStreet("123 Main St");
        testAddressDto.setCity("New York");
        testAddressDto.setState("NY");
        testAddressDto.setZipCode("10001");
        testAddressDto.setCountry("USA");
        testAddressDto.setDefault(true);

        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setId(1L);
        testPaymentMethod.setType(PaymentType.CREDIT_CARD);
        testPaymentMethod.setCardNumber("1234567890123456");
        testPaymentMethod.setExpirationDate("12/25");
        testPaymentMethod.setCardHolderName("John Doe");

        testPaymentMethodDto = new PaymentMethodDto();
        testPaymentMethodDto.setId(1L);
        testPaymentMethodDto.setType(PaymentType.CREDIT_CARD);
        testPaymentMethodDto.setCardNumber("1234567890123456");
        testPaymentMethodDto.setExpirationDate("12/25");
        testPaymentMethodDto.setCardHolderName("John Doe");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setFirstName("John");
        testAccount.setLastName("Doe");
        testAccount.setAddresses(new ArrayList<>(List.of(testAddress)));
        testAccount.setPaymentMethods(new ArrayList<>(List.of(testPaymentMethod)));

        testAccountDto = new AccountDto();
        testAccountDto.setId(1L);
        testAccountDto.setEmail("test@example.com");
        testAccountDto.setFirstName("John");
        testAccountDto.setLastName("Doe");
        testAccountDto.setAddresses(List.of(testAddressDto));
        testAccountDto.setPaymentMethods(List.of(testPaymentMethodDto));
    }

    @Test
    void getAccount_WhenAccountExists_ShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccount(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccount_WhenAccountNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccount(999L);
        });

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findById(999L);
    }

    @Test
    void createAccount_WithValidData_ShouldCreateAccount() {
        Account newAccount = new Account();
        newAccount.setEmail("new@example.com");
        newAccount.setFirstName("Jane");
        newAccount.setLastName("Smith");
        newAccount.setAddresses(new ArrayList<>(List.of(testAddress)));
        newAccount.setPaymentMethods(new ArrayList<>(List.of(testPaymentMethod)));

        AccountDto newAccountDto = new AccountDto();
        newAccountDto.setEmail("new@example.com");
        newAccountDto.setFirstName("Jane");
        newAccountDto.setLastName("Smith");
        newAccountDto.setAddresses(List.of(testAddressDto));
        newAccountDto.setPaymentMethods(List.of(testPaymentMethodDto));

        when(accountMapper.toEntity(newAccountDto)).thenReturn(newAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.createAccount(newAccountDto);

        assertNotNull(result);
        verify(accountMapper, times(1)).toEntity(newAccountDto);
        verify(accountRepository, times(1)).save(any(Account.class));
        assertNull(newAccount.getId());
        assertNull(newAccount.getAddresses().get(0).getId());
        assertNull(newAccount.getPaymentMethods().get(0).getId());
    }

    @Test
    void createAccount_WithNullAddressesAndPaymentMethods_ShouldCreateAccount() {
        AccountDto newAccountDto = new AccountDto();
        newAccountDto.setEmail("new@example.com");
        newAccountDto.setFirstName("Jane");
        newAccountDto.setLastName("Smith");
        newAccountDto.setAddresses(null);
        newAccountDto.setPaymentMethods(null);

        Account newAccount = new Account();
        newAccount.setEmail("new@example.com");
        newAccount.setFirstName("Jane");
        newAccount.setLastName("Smith");

        when(accountMapper.toEntity(newAccountDto)).thenReturn(newAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.createAccount(newAccountDto);

        assertNotNull(result);
        verify(accountMapper, times(1)).toEntity(newAccountDto);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateAccount_WithValidData_ShouldUpdateAccount() {
        AccountDto updateDto = new AccountDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");
        updateDto.setEmail("updated@example.com");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated", testAccount.getFirstName());
        assertEquals("Name", testAccount.getLastName());
        assertEquals("updated@example.com", testAccount.getEmail());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithNullFields_ShouldNotUpdateThoseFields() {
        AccountDto updateDto = new AccountDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName(null);
        updateDto.setEmail(null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated", testAccount.getFirstName());
        assertEquals("Doe", testAccount.getLastName());
        assertEquals("test@example.com", testAccount.getEmail());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithNewAddress_ShouldAddAddress() {
        AddressDto newAddressDto = new AddressDto();
        newAddressDto.setStreet("456 New St");
        newAddressDto.setCity("Boston");
        newAddressDto.setState("MA");
        newAddressDto.setZipCode("02101");
        newAddressDto.setCountry("USA");
        newAddressDto.setDefault(false);

        AccountDto updateDto = new AccountDto();
        updateDto.setAddresses(List.of(newAddressDto));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(1, testAccount.getAddresses().size());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithExistingAddressUpdate_ShouldUpdateAddress() {
        AddressDto updateAddressDto = new AddressDto();
        updateAddressDto.setId(1L);
        updateAddressDto.setStreet("789 Updated St");
        updateAddressDto.setCity("Los Angeles");
        updateAddressDto.setState("CA");
        updateAddressDto.setZipCode("90210");
        updateAddressDto.setCountry("USA");
        updateAddressDto.setDefault(false);

        AccountDto updateDto = new AccountDto();
        updateDto.setAddresses(List.of(updateAddressDto));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(1, testAccount.getAddresses().size());
        assertEquals("789 Updated St", testAccount.getAddresses().get(0).getStreet());
        assertEquals("Los Angeles", testAccount.getAddresses().get(0).getCity());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithAddressRemoval_ShouldRemoveAddress() {
        AccountDto updateDto = new AccountDto();
        updateDto.setAddresses(new ArrayList<>());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(0, testAccount.getAddresses().size());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithNewPaymentMethod_ShouldAddPaymentMethod() {
        PaymentMethodDto newPaymentDto = new PaymentMethodDto();
        newPaymentDto.setType(PaymentType.PAYPAL);
        newPaymentDto.setCardNumber("9876543210987654");
        newPaymentDto.setExpirationDate("06/26");
        newPaymentDto.setCardHolderName("Jane Smith");

        AccountDto updateDto = new AccountDto();
        updateDto.setPaymentMethods(List.of(newPaymentDto));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(1, testAccount.getPaymentMethods().size());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithExistingPaymentMethodUpdate_ShouldUpdatePaymentMethod() {
        PaymentMethodDto updatePaymentDto = new PaymentMethodDto();
        updatePaymentDto.setId(1L);
        updatePaymentDto.setType(PaymentType.DEBIT_CARD);
        updatePaymentDto.setCardNumber("1111222233334444");
        updatePaymentDto.setExpirationDate("08/27");
        updatePaymentDto.setCardHolderName("Updated Name");

        AccountDto updateDto = new AccountDto();
        updateDto.setPaymentMethods(List.of(updatePaymentDto));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(1, testAccount.getPaymentMethods().size());
        assertEquals(PaymentType.DEBIT_CARD, testAccount.getPaymentMethods().get(0).getType());
        assertEquals("1111222233334444", testAccount.getPaymentMethods().get(0).getCardNumber());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithPaymentMethodRemoval_ShouldRemovePaymentMethod() {
        AccountDto updateDto = new AccountDto();
        updateDto.setPaymentMethods(new ArrayList<>());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals(0, testAccount.getPaymentMethods().size());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void updateAccount_WithNullAddressesAndPaymentMethods_ShouldNotThrowException() {
        AccountDto updateDto = new AccountDto();
        updateDto.setFirstName("Updated");
        updateDto.setAddresses(null);
        updateDto.setPaymentMethods(null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.updateAccount(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated", testAccount.getFirstName());
        assertEquals(1, testAccount.getAddresses().size());
        assertEquals(1, testAccount.getPaymentMethods().size());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void deleteAccount_WithValidId_ShouldDeleteAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).delete(testAccount);

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    void deleteAccount_WhenAccountNotFound_ShouldThrowRuntimeException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(999L);
        });

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findById(999L);
        verify(accountRepository, never()).delete(any());
    }
}
