package com.bookstore.gateway.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityMonitoringServiceTest {
    
    private SecurityMonitoringService service;
    private MeterRegistry meterRegistry;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new SecurityMonitoringService(meterRegistry);
    }
    
    @Test
    void shouldRecordAuthenticationSuccess() {
        service.recordAuthenticationSuccess("user123");
        
        Counter counter = meterRegistry.counter("security.authentication.successes");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void shouldRecordAuthenticationFailure() {
        service.recordAuthenticationFailure("user123", "Invalid credentials");
        
        Counter counter = meterRegistry.counter("security.authentication.failures");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void shouldRecordSuspiciousActivity() {
        service.recordSuspiciousActivity("192.168.1.1", "Multiple failed attempts");
        
        Counter counter = meterRegistry.counter("security.suspicious.activities");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void shouldDetectMultipleFailedAttempts() {
        String identifier = "user123";
        
        // Record 5 failed attempts
        for (int i = 0; i < 5; i++) {
            service.recordAuthenticationFailure(identifier, "Invalid credentials");
        }
        
        Counter failureCounter = meterRegistry.counter("security.authentication.failures");
        Counter suspiciousCounter = meterRegistry.counter("security.suspicious.activities");
        
        assertEquals(5.0, failureCounter.count());
        assertTrue(suspiciousCounter.count() >= 1.0);
    }
    
    @Test
    void shouldResetFailedAttemptsAfterSuccess() {
        String identifier = "user123";
        
        // Record failed attempts
        service.recordAuthenticationFailure(identifier, "Invalid credentials");
        service.recordAuthenticationFailure(identifier, "Invalid credentials");
        
        // Record success
        service.recordAuthenticationSuccess(identifier);
        
        Counter successCounter = meterRegistry.counter("security.authentication.successes");
        assertEquals(1.0, successCounter.count());
    }
    
    @Test
    void shouldRecordUnauthorizedAccess() {
        service.recordUnauthorizedAccess("test-service", "/api/admin");
        
        Counter counter = meterRegistry.counter("security.suspicious.activities");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void shouldRecordInvalidServiceCredentials() {
        service.recordInvalidServiceCredentials("malicious-service");
        
        Counter failureCounter = meterRegistry.counter("security.authentication.failures");
        Counter suspiciousCounter = meterRegistry.counter("security.suspicious.activities");
        
        assertEquals(1.0, failureCounter.count());
        assertEquals(1.0, suspiciousCounter.count());
    }
}
