package com.github.xujia118.authservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "auth_users")
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // This will store the BCRYPT HASH, never plain text

    private boolean enabled = true; // For locking accounts

    // For security auditing
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;
}
