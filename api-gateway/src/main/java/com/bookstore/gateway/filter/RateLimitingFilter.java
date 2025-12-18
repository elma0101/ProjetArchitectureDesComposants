package com.bookstore.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientId(exchange);
            String key = "rate_limit:" + clientId;
            
            return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // Set expiration for the first request
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                            .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > config.getRequestsPerMinute()) {
                        return onError(exchange, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                    }
                    
                    // Add rate limit headers
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getRequestsPerMinute()));
                    response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, config.getRequestsPerMinute() - count)));
                    
                    return chain.filter(exchange);
                })
                .onErrorResume(throwable -> {
                    // If Redis is unavailable, allow the request to proceed
                    return chain.filter(exchange);
                });
        };
    }

    private String getClientId(ServerWebExchange exchange) {
        // Use IP address as client identifier
        String clientIp = exchange.getRequest().getRemoteAddress() != null 
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
        
        // If user is authenticated, use username instead
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        return userId != null ? userId : clientIp;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"%s\", \"status\": %d}", err, httpStatus.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        private int requestsPerMinute = 100; // Default rate limit

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}