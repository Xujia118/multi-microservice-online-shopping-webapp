package com.github.xujia118.authservice.service;

import com.github.xujia118.authservice.controller.AuthResponse;
import com.github.xujia118.authservice.model.Auth;
import com.github.xujia118.authservice.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public void register(String email, String password) {
        // 1. Check if user exists
        if (authRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // 2. Hash the password and save
        Auth newUser = new Auth();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // BCRYPT
        authRepository.save(newUser);
        log.info("User registered successfully: {}", email);
    }

    public AuthResponse login(String email, String password) {
        // 1. Find the user
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
