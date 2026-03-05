package com.github.xujia118.gatewayservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    private final Logger logger = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // PRE-FILTER LOGIC: Happens before the request is routed
        logger.info("Global Filter: Request Path is {}", exchange.getRequest().getPath());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // POST-FILTER LOGIC: Happens after the service responds
            logger.info("Global Filter: Response Status Code is {}", exchange.getResponse().getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
