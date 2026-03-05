package com.github.xujia118.gatewayservice.config;

import com.github.xujia118.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

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
        // This connects Spring Security's internal logic to your JwtUtil logic
        return token -> {
            try {
                // Use your existing validate/extract logic
                jwtUtil.validateToken(token);
                String username = jwtUtil.extractUsername(token);

                // Return a standard JWT object that Spring understands
                return Mono.just(org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                        .header("alg", "HS256")
                        .subject(username)
                        .build());
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }
}