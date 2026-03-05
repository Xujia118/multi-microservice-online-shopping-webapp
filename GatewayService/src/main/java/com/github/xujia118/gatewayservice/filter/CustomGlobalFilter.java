package com.github.xujia118.gatewayservice.filter;

import com.github.xujia118.gatewayservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    // 'sub' is the email
                    String email = auth.getName();
                    // Extract our custom 'userId' claim from the token
                    Object userId = auth.getTokenAttributes().get("userId");

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r
                                    .header("X-User-Email", email)
                                    .header("X-User-Id", String.valueOf(userId))
                            ).build();

                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange)); // Fallback for permitAll routes
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
