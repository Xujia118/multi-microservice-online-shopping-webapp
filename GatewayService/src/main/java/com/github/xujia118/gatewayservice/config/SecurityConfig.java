package com.github.xujia118.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for development
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/items/**").permitAll() // Explicitly permit items
                        .pathMatchers("/auth/**").permitAll()         // Explicitly permit auth
                        .anyExchange().permitAll()                   // Permit everything else for now
                )
                .build();
    }
}