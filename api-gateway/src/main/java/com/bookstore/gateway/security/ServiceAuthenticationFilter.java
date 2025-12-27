package com.bookstore.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter that adds service authentication headers to internal service calls
 */
@Component
public class ServiceAuthenticationFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthenticationFilter.class);
    private static final String SERVICE_API_KEY_HEADER = "X-Service-API-Key";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    
    @Value("${security.service.api-key}")
    private String serviceApiKey;
    
    @Value("${spring.application.name}")
    private String serviceName;
    
    private static final List<String> INTERNAL_SERVICE_PATHS = List.of(
        "/api/books",
        "/api/users",
        "/api/loans",
        "/api/recommendations",
        "/api/notifications",
        "/api/audit"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Add service authentication headers for internal service calls
        if (isInternalServiceCall(path)) {
            exchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.add(SERVICE_API_KEY_HEADER, serviceApiKey);
                    headers.add(SERVICE_NAME_HEADER, serviceName);
                }))
                .build();
            
            logger.debug("Added service authentication headers for path: {}", path);
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isInternalServiceCall(String path) {
        return INTERNAL_SERVICE_PATHS.stream()
            .anyMatch(path::startsWith);
    }
    
    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}
