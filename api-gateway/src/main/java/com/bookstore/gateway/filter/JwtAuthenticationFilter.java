package com.bookstore.gateway.filter;

import com.bookstore.gateway.client.UserServiceClient;
import com.bookstore.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceClient userServiceClient;

    @Value("${gateway.auth.use-remote-validation:false}")
    private boolean useRemoteValidation;

    private static final List<String> EXCLUDED_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/health",
        "/actuator",
        "/fallback"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip authentication for excluded paths
            if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            // Use remote validation if enabled, otherwise fall back to local validation
            if (useRemoteValidation) {
                return validateTokenRemotely(token, exchange, chain);
            } else {
                return validateTokenLocally(token, exchange, chain);
            }
        };
    }

    /**
     * Validate token locally using JwtUtil
     */
    private Mono<Void> validateTokenLocally(String token, ServerWebExchange exchange, 
                                            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information and add to headers
            String username = jwtUtil.getUsernameFromToken(token);
            String roles = jwtUtil.getRolesFromToken(token);
            
            logger.debug("Token validated locally for user: {}", username);
            
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", username)
                .header("X-User-Roles", roles)
                .header("X-Auth-Method", "local")
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            logger.error("Local token validation failed", e);
            return onError(exchange, "JWT token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Validate token remotely using User Management Service
     * Falls back to local validation if remote service is unavailable
     */
    private Mono<Void> validateTokenRemotely(String token, ServerWebExchange exchange,
                                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return userServiceClient.validateToken(token)
            .flatMap(validationResponse -> {
                if (validationResponse.isValid()) {
                    logger.debug("Token validated remotely for user: {}", validationResponse.getUsername());
                    
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", validationResponse.getUsername())
                        .header("X-User-Roles", validationResponse.getRoles())
                        .header("X-Auth-Method", "remote")
                        .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }
            })
            .switchIfEmpty(Mono.defer(() -> {
                // Fallback to local validation if remote service is unavailable
                logger.warn("Remote token validation unavailable, falling back to local validation");
                return validateTokenLocally(token, exchange, chain);
            }))
            .onErrorResume(e -> {
                // Fallback to local validation on error
                logger.error("Error during remote token validation, falling back to local validation", e);
                return validateTokenLocally(token, exchange, chain);
            });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"%s\", \"status\": %d, \"timestamp\": \"%s\"}", 
            err, httpStatus.value(), java.time.Instant.now().toString());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties if needed
    }
}