package com.bookstore.catalog.client;

import com.bookstore.catalog.dto.UserResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for User Management Service with graceful degradation
 * Returns null or false to allow catalog operations to continue with limited functionality
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    private final Counter fallbackCounter;

    @Autowired
    public UserServiceClientFallback(MeterRegistry meterRegistry) {
        this.fallbackCounter = Counter.builder("user_service_fallback")
            .description("Number of times User Management Service fallback was triggered")
            .tag("service", "user-management")
            .register(meterRegistry);
    }

    @Override
    public UserResponse getUserById(Long id, String token) {
        log.warn("Fallback: Unable to fetch user with id: {}. Returning null for graceful degradation.", id);
        fallbackCounter.increment();
        
        // Graceful degradation: return null to allow catalog operations to continue
        // without user details
        return null;
    }

    @Override
    public Boolean userExists(Long id, String token) {
        log.warn("Fallback: Unable to check if user exists with id: {}. Returning false for graceful degradation.", id);
        fallbackCounter.increment();
        
        // Graceful degradation: return false to prevent operations that require user validation
        // This is safer than returning true
        return false;
    }
}
