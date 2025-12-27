package com.bookstore.usermanagement.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for monitoring security events and potential threats
 */
@Service
public class SecurityMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitoringService.class);
    private static final int FAILED_AUTH_THRESHOLD = 5;
    private static final int TIME_WINDOW_MINUTES = 5;
    
    private final Counter authenticationFailures;
    private final Counter authenticationSuccesses;
    private final Counter suspiciousActivities;
    private final Counter serviceAuthFailures;
    
    private final Map<String, FailedAuthAttempt> failedAuthAttempts = new ConcurrentHashMap<>();
    
    public SecurityMonitoringService(MeterRegistry meterRegistry) {
        this.authenticationFailures = Counter.builder("security.authentication.failures")
            .description("Number of failed authentication attempts")
            .register(meterRegistry);
        
        this.authenticationSuccesses = Counter.builder("security.authentication.successes")
            .description("Number of successful authentication attempts")
            .register(meterRegistry);
        
        this.suspiciousActivities = Counter.builder("security.suspicious.activities")
            .description("Number of suspicious security activities detected")
            .register(meterRegistry);
        
        this.serviceAuthFailures = Counter.builder("security.service.auth.failures")
            .description("Number of failed service-to-service authentication attempts")
            .register(meterRegistry);
    }
    
    public void recordAuthenticationSuccess(String identifier) {
        authenticationSuccesses.increment();
        failedAuthAttempts.remove(identifier);
        logger.debug("Authentication success recorded for: {}", identifier);
    }
    
    public void recordAuthenticationFailure(String identifier, String reason) {
        authenticationFailures.increment();
        
        FailedAuthAttempt attempt = failedAuthAttempts.computeIfAbsent(
            identifier, 
            k -> new FailedAuthAttempt()
        );
        
        attempt.increment();
        
        if (attempt.getCount() >= FAILED_AUTH_THRESHOLD) {
            recordSuspiciousActivity(identifier, "Multiple failed authentication attempts");
            logger.warn("SECURITY ALERT: Multiple failed authentication attempts from: {} (count: {})", 
                identifier, attempt.getCount());
        }
        
        logger.info("Authentication failure recorded for: {} - Reason: {}", identifier, reason);
    }
    
    public void recordSuspiciousActivity(String identifier, String description) {
        suspiciousActivities.increment();
        logger.warn("SECURITY ALERT: Suspicious activity detected - Identifier: {}, Description: {}", 
            identifier, description);
    }
    
    public void recordServiceAuthFailure(String serviceName, String path) {
        serviceAuthFailures.increment();
        suspiciousActivities.increment();
        logger.warn("SECURITY ALERT: Service authentication failure - Service: {}, Path: {}", 
            serviceName, path);
    }
    
    private static class FailedAuthAttempt {
        private final AtomicInteger count = new AtomicInteger(0);
        private LocalDateTime firstAttempt = LocalDateTime.now();
        
        public void increment() {
            LocalDateTime now = LocalDateTime.now();
            if (firstAttempt.plusMinutes(TIME_WINDOW_MINUTES).isBefore(now)) {
                // Reset if outside time window
                count.set(1);
                firstAttempt = now;
            } else {
                count.incrementAndGet();
            }
        }
        
        public int getCount() {
            return count.get();
        }
    }
}
