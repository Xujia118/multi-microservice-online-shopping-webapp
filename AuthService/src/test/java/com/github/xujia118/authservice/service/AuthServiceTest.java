package com.github.xujia118.authservice.service;

import com.github.xujia118.authservice.client.AccountClient;
import com.github.xujia118.authservice.controller.AuthResponse;
import com.github.xujia118.authservice.model.Auth;
import com.github.xujia118.authservice.repository.AuthRepository;
import com.github.xujia118.common.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private AuthService authService;

    private Auth testAuth;

    @BeforeEach
    void setUp() {
        testAuth = new Auth();
        testAuth.setId(1L);
        testAuth.setEmail("test@example.com");
        testAuth.setPassword("encodedPassword");
    }

    @Test
    void register_WithNewUser_ShouldCreateUserAndAccount() {
        String email = "newuser@example.com";
        String password = "password123";

        when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(authRepository.save(any(Auth.class))).thenReturn(testAuth);
        doNothing().when(accountClient).createAccount(any(AccountDto.class));

        authService.register(email, password);

        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(authRepository, times(1)).save(any(Auth.class));
        verify(accountClient, times(1)).createAccount(any(AccountDto.class));
    }

    @Test
    void register_WithExistingUser_ShouldThrowRuntimeException() {
        String email = "existing@example.com";
        String password = "password123";

        when(authRepository.findByEmail(email)).thenReturn(Optional.of(testAuth));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(email, password);
        });

        assertEquals("User already exists with email: " + email, exception.getMessage());
        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(authRepository, never()).save(any(Auth.class));
        verify(accountClient, never()).createAccount(any(AccountDto.class));
    }

    @Test
    void register_WhenAccountClientFails_ShouldThrowRuntimeException() {
        String email = "newuser@example.com";
        String password = "password123";

        when(authRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(authRepository.save(any(Auth.class))).thenReturn(testAuth);
        doThrow(new RuntimeException("Account service error")).when(accountClient).createAccount(any(AccountDto.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(email, password);
        });

        assertEquals("Registration failed due to profile creation error.", exception.getMessage());
        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(authRepository, times(1)).save(any(Auth.class));
        verify(accountClient, times(1)).createAccount(any(AccountDto.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt.token.here";

        when(authRepository.findByEmail(email)).thenReturn(Optional.of(testAuth));
        when(passwordEncoder.matches(password, testAuth.getPassword())).thenReturn(true);
        when(jwtService.generateToken(email, testAuth.getId())).thenReturn(token);

        AuthResponse response = authService.login(email, password);

        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(testAuth.getId(), response.userId());
        assertEquals(testAuth.getEmail(), response.email());
        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testAuth.getPassword());
        verify(jwtService, times(1)).generateToken(email, testAuth.getId());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowRuntimeException() {
        String email = "nonexistent@example.com";
        String password = "password123";

        when(authRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, password);
        });

        assertEquals("User not found", exception.getMessage());
        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyLong());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowRuntimeException() {
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authRepository.findByEmail(email)).thenReturn(Optional.of(testAuth));
        when(passwordEncoder.matches(password, testAuth.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, testAuth.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyLong());
    }
}
