package com.github.xujia118.authservice.service;

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

    public String register(String email, String password) {
        // 1. Check if user exists
        if (authRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // 2. Hash the password and save
        Auth newUser = new Auth();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // BCRYPT
        authRepository.save(newUser);

        // 3. Generate token for immediate login after registration
        return jwtService.generateToken(email);
    }

    public String login(String email, String password) {
        // 1. Find the user
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Verify password
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtService.generateToken(email);
        }

        throw new RuntimeException("Invalid credentials");
    }
}
