package com.bookstore.recommendation.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for monitoring Resilience4j circuit breakers, retries, and rate limiters
 */
@RestController
@RequestMapping("/api/resilience")
@RequiredArgsConstructor
@Slf4j
public class ResilienceMonitoringController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    @GetMapping("/circuit-breakers")
    public ResponseEntity<Map<String, Object>> getCircuitBreakers() {
        Map<String, Object> circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                cb -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("state", cb.getState().toString());
                    details.put("failureRate", cb.getMetrics().getFailureRate());
                    details.put("slowCallRate", cb.getMetrics().getSlowCallRate());
                    details.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                    details.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
                    details.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
                    return details;
                }
            ));
        
        return ResponseEntity.ok(circuitBreakers);
    }

    @GetMapping("/circuit-breakers/{name}")
    public ResponseEntity<Map<String, Object>> getCircuitBreaker(@PathVariable String name) {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
            Map<String, Object> details = new HashMap<>();
            details.put("name", cb.getName());
            details.put("state", cb.getState().toString());
            details.put("failureRate", cb.getMetrics().getFailureRate());
            details.put("slowCallRate", cb.getMetrics().getSlowCallRate());
            details.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            details.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            details.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Circuit breaker not found: {}", name);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/circuit-breakers/{name}/reset")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker(@PathVariable String name) {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
            cb.reset();
            log.info("Circuit breaker {} has been reset", name);
            return ResponseEntity.ok(Map.of("message", "Circuit breaker reset successfully", "name", name));
        } catch (Exception e) {
            log.error("Failed to reset circuit breaker: {}", name, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to reset circuit breaker"));
        }
    }

    @GetMapping("/retries")
    public ResponseEntity<Map<String, Object>> getRetries() {
        Map<String, Object> retries = retryRegistry.getAllRetries()
            .stream()
            .collect(Collectors.toMap(
                Retry::getName,
                retry -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("numberOfSuccessfulCallsWithoutRetryAttempt", 
                        retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
                    details.put("numberOfSuccessfulCallsWithRetryAttempt", 
                        retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
                    details.put("numberOfFailedCallsWithoutRetryAttempt", 
                        retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
                    details.put("numberOfFailedCallsWithRetryAttempt", 
                        retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
                    return details;
                }
            ));
        
        return ResponseEntity.ok(retries);
    }

    @GetMapping("/rate-limiters")
    public ResponseEntity<Map<String, Object>> getRateLimiters() {
        Map<String, Object> rateLimiters = rateLimiterRegistry.getAllRateLimiters()
            .stream()
            .collect(Collectors.toMap(
                RateLimiter::getName,
                rl -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("availablePermissions", rl.getMetrics().getAvailablePermissions());
                    details.put("numberOfWaitingThreads", rl.getMetrics().getNumberOfWaitingThreads());
                    return details;
                }
            ));
        
        return ResponseEntity.ok(rateLimiters);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getResilienceHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Circuit breaker health
        Map<String, String> cbHealth = circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                cb -> cb.getState().toString()
            ));
        health.put("circuitBreakers", cbHealth);
        
        // Overall status
        boolean allHealthy = cbHealth.values().stream()
            .allMatch(state -> "CLOSED".equals(state) || "HALF_OPEN".equals(state));
        health.put("status", allHealthy ? "HEALTHY" : "DEGRADED");
        
        return ResponseEntity.ok(health);
    }
}
