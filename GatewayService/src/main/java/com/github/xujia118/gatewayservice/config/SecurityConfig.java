package com.github.xujia118.gatewayservice.config;

import com.github.xujia118.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for development
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/auth/**").permitAll()         // Explicitly permit auth
                        .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/items/**").permitAll() // Browsing is public
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())) // If using a local JwtDecoder
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // The exact same secret from your AuthService
        byte[] keyBytes = Base64.getDecoder().decode(JwtUtil.SECRET);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // This automatically validates the signature and extracts ALL claims (including userId, iat, exp)
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }
}