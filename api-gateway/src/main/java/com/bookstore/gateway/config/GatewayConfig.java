package com.bookstore.gateway.config;

import com.bookstore.gateway.filter.JwtAuthenticationFilter;
import com.bookstore.gateway.filter.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Management Service Routes
            .route("user-service-auth", r -> r
                .path("/api/auth/**")
                .and()
                .method(HttpMethod.POST)
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("user-service")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://user-management-service"))
            
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("user-service")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://user-management-service"))

            // Book Catalog Service Routes
            .route("book-service", r -> r
                .path("/api/books/**", "/api/authors/**", "/api/categories/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("book-service")
                        .setFallbackUri("forward:/fallback/book-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://book-catalog-service"))

            // Loan Management Service Routes
            .route("loan-service", r -> r
                .path("/api/loans/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("loan-service")
                        .setFallbackUri("forward:/fallback/loan-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://loan-management-service"))

            // Recommendation Service Routes
            .route("recommendation-service", r -> r
                .path("/api/recommendations/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("recommendation-service")
                        .setFallbackUri("forward:/fallback/recommendation-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://recommendation-service"))

            // Audit Service Routes
            .route("audit-service", r -> r
                .path("/api/audit/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("audit-service")
                        .setFallbackUri("forward:/fallback/audit-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://audit-service"))

            // Notification Service Routes
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .circuitBreaker(config -> config
                        .setName("notification-service")
                        .setFallbackUri("forward:/fallback/notification-service"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(1000), 2, false)))
                .uri("lb://notification-service"))

            // Fallback for monolith during migration
            .route("monolith-fallback", r -> r
                .path("/api/**")
                .filters(f -> f
                    .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config())))
                .uri("lb://bookstore-monolith"))

            .build();
    }
}