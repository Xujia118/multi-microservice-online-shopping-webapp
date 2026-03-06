package com.github.xujia118.authservice.service;

import com.github.xujia118.authservice.client.AccountClient;
import com.github.xujia118.authservice.controller.AuthResponse;
import com.github.xujia118.authservice.model.Auth;
import com.github.xujia118.authservice.repository.AuthRepository;
import com.github.xujia118.common.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccountClient accountClient;

    @Transactional
    public void register(String email, String password) {
        // 1. Check if user exists
        if (authRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // 2. Hash the password and save
        Auth newUser = new Auth();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        Auth savedAuth = authRepository.save(newUser);
        log.info("User saved in Auth DB with ID: {}", savedAuth.getId());

        // 3. Create Account Placeholder in AccountService
        try {
            AccountDto placeholder = AccountDto.builder()
                    .id(savedAuth.getId())
                    .email(savedAuth.getEmail())
                    .build();

            accountClient.createAccount(placeholder);
            log.info("Account placeholder created successfully for: {}", email);
        } catch (Exception e) {
            log.error("Failed to create account placeholder for {}. Rolling back...", email, e);
            // Throwing an exception here triggers the @Transactional rollback
            // so you don't end up with an Auth record but no Account record.
            throw new RuntimeException("Registration failed due to profile creation error.");
        }
    }

    public AuthResponse login(String email, String password) {
        // 1. Find the user
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Should set jwt in the header, to check
        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
