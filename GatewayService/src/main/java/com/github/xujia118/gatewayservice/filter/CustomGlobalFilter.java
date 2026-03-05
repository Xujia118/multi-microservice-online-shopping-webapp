package com.github.xujia118.gatewayservice.filter;

import com.github.xujia118.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // PRE-FILTER LOGIC: Happens before the request is routed
        String path = exchange.getRequest().getPath().toString();
        logger.info("Global Filter: Request Path is {}", path);

        // 1. Skip validation for Auth Service (Login/Register)
        if (path.contains("/api/v1/auth") || path.contains("/api/v1/items")) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            // 3. Validate Token and extract the user info
            String username = jwtUtil.extractUsername(token);
            jwtUtil.validateToken(token);

            // 4. "Mutate" the request to add a custom header for downstream services
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Email", username))
                    .build();

            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            logger.error("JWT Validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
