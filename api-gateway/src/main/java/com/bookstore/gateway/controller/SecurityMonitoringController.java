package com.bookstore.gateway.controller;

import com.bookstore.gateway.security.SecurityMonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for security monitoring and metrics
 */
@RestController
@RequestMapping("/api/security")
public class SecurityMonitoringController {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("authenticationFailures", 
            meterRegistry.counter("security.authentication.failures").count());
        metrics.put("authenticationSuccesses", 
            meterRegistry.counter("security.authentication.successes").count());
        metrics.put("suspiciousActivities", 
            meterRegistry.counter("security.suspicious.activities").count());
        
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getSecurityHealth() {
        Map<String, String> health = new HashMap<>();
        
        double failureCount = meterRegistry.counter("security.authentication.failures").count();
        double suspiciousCount = meterRegistry.counter("security.suspicious.activities").count();
        
        String status = "HEALTHY";
        if (suspiciousCount > 10) {
            status = "WARNING";
        }
        if (suspiciousCount > 50 || failureCount > 100) {
            status = "CRITICAL";
        }
        
        health.put("status", status);
        health.put("message", "Security monitoring active");
        
        return ResponseEntity.ok(health);
    }
}
