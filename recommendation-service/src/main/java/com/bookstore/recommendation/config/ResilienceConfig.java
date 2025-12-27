package com.bookstore.recommendation.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Resilience4j patterns including circuit breakers, retry, and time limiters
 */
@Configuration
@Slf4j
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // Add event listeners for monitoring
        registry.circuitBreaker("bookCatalogService").getEventPublisher()
            .onStateTransition(event -> 
                log.warn("Circuit Breaker State Transition: {} -> {} for {}",
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState(),
                    event.getCircuitBreakerName()))
            .onError(event -> 
                log.error("Circuit Breaker Error: {} - {}",
                    event.getCircuitBreakerName(),
                    event.getThrowable().getMessage()))
            .onSuccess(event -> 
                log.debug("Circuit Breaker Success: {}", event.getCircuitBreakerName()));
        
        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();
        
        // Add event listeners for retry monitoring
        registry.retry("bookCatalogService").getEventPublisher()
            .onRetry(event -> 
                log.warn("Retry attempt {} for {} - {}",
                    event.getNumberOfRetryAttempts(),
                    event.getName(),
                    event.getLastThrowable().getMessage()))
            .onError(event -> 
                log.error("Retry failed after {} attempts for {}",
                    event.getNumberOfRetryAttempts(),
                    event.getName()));
        
        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();
        
        // Add event listeners for timeout monitoring
        registry.timeLimiter("bookCatalogService").getEventPublisher()
            .onTimeout(event -> 
                log.error("Time Limiter Timeout: {}", event.getTimeLimiterName()))
            .onError(event -> 
                log.error("Time Limiter Error: {} - {}",
                    event.getTimeLimiterName(),
                    event.getThrowable().getMessage()));
        
        return registry;
    }
}
