package com.bookstore.controller;

import com.bookstore.service.MonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for monitoring and observability endpoints
 */
@RestController
@RequestMapping("/api/admin/monitoring")
@Tag(name = "Monitoring", description = "Monitoring and observability endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final MeterRegistry meterRegistry;
    private final HealthEndpoint healthEndpoint;

    @Autowired
    public MonitoringController(MonitoringService monitoringService, 
                              MeterRegistry meterRegistry,
                              HealthEndpoint healthEndpoint) {
        this.monitoringService = monitoringService;
        this.meterRegistry = meterRegistry;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health/detailed")
    @Operation(summary = "Get detailed health information")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        HealthComponent health = healthEndpoint.health();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/summary")
    @Operation(summary = "Get application metrics summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> metrics = new HashMap<>();
        
        // HTTP metrics
        io.micrometer.core.instrument.Timer httpTimer = meterRegistry.find("http.server.requests").timer();
        if (httpTimer != null) {
            metrics.put("http_requests_total", httpTimer.count());
            metrics.put("http_requests_mean_duration", httpTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            metrics.put("http_requests_max_duration", httpTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
        
        // Custom business metrics
        io.micrometer.core.instrument.Counter loanCounter = meterRegistry.find("bookstore.loans.total").counter();
        if (loanCounter != null) {
            metrics.put("total_loans", loanCounter.count());
        }
        
        io.micrometer.core.instrument.Counter returnCounter = meterRegistry.find("bookstore.returns.total").counter();
        if (returnCounter != null) {
            metrics.put("total_returns", returnCounter.count());
        }
        
        io.micrometer.core.instrument.Counter registrationCounter = meterRegistry.find("bookstore.users.registrations").counter();
        if (registrationCounter != null) {
            metrics.put("total_registrations", registrationCounter.count());
        }
        
        io.micrometer.core.instrument.Counter errorCounter = meterRegistry.find("bookstore.api.errors").counter();
        if (errorCounter != null) {
            metrics.put("total_errors", errorCounter.count());
        }
        
        // Gauge metrics
        io.micrometer.core.instrument.Gauge activeUsersGauge = meterRegistry.find("bookstore.users.active").gauge();
        if (activeUsersGauge != null) {
            metrics.put("active_users", activeUsersGauge.value());
        }
        
        io.micrometer.core.instrument.Gauge currentLoansGauge = meterRegistry.find("bookstore.loans.current").gauge();
        if (currentLoansGauge != null) {
            metrics.put("current_loans", currentLoansGauge.value());
        }
        
        // JVM metrics
        io.micrometer.core.instrument.Gauge memoryGauge = meterRegistry.find("jvm.memory.used").gauge();
        if (memoryGauge != null) {
            metrics.put("jvm_memory_used", memoryGauge.value());
        }
        
        io.micrometer.core.instrument.Gauge threadsGauge = meterRegistry.find("jvm.threads.live").gauge();
        if (threadsGauge != null) {
            metrics.put("jvm_threads", threadsGauge.value());
        }
        
        metrics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/metrics/business")
    @Operation(summary = "Get business-specific metrics")
    public ResponseEntity<Map<String, Object>> getBusinessMetrics() {
        Map<String, Object> businessMetrics = new HashMap<>();
        
        // Loan metrics
        Map<String, Object> loanMetrics = new HashMap<>();
        io.micrometer.core.instrument.Counter loanCounter = meterRegistry.find("bookstore.loans.total").counter();
        if (loanCounter != null) {
            loanMetrics.put("total", loanCounter.count());
        }
        
        io.micrometer.core.instrument.Counter returnCounter = meterRegistry.find("bookstore.returns.total").counter();
        if (returnCounter != null) {
            loanMetrics.put("returns", returnCounter.count());
        }
        
        io.micrometer.core.instrument.Gauge currentLoansGauge = meterRegistry.find("bookstore.loans.current").gauge();
        if (currentLoansGauge != null) {
            loanMetrics.put("active", currentLoansGauge.value());
        }
        
        businessMetrics.put("loans", loanMetrics);
        
        // User metrics
        Map<String, Object> userMetrics = new HashMap<>();
        io.micrometer.core.instrument.Counter registrationCounter = meterRegistry.find("bookstore.users.registrations").counter();
        if (registrationCounter != null) {
            userMetrics.put("registrations", registrationCounter.count());
        }
        
        io.micrometer.core.instrument.Gauge activeUsersGauge = meterRegistry.find("bookstore.users.active").gauge();
        if (activeUsersGauge != null) {
            userMetrics.put("active", activeUsersGauge.value());
        }
        
        businessMetrics.put("users", userMetrics);
        
        // Search metrics
        Map<String, Object> searchMetrics = new HashMap<>();
        io.micrometer.core.instrument.Timer searchTimer = meterRegistry.find("bookstore.search.duration").timer();
        if (searchTimer != null) {
            searchMetrics.put("total_searches", searchTimer.count());
            searchMetrics.put("average_duration_ms", searchTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            searchMetrics.put("max_duration_ms", searchTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
        
        businessMetrics.put("search", searchMetrics);
        
        // Error metrics
        Map<String, Object> errorMetrics = new HashMap<>();
        io.micrometer.core.instrument.Counter errorCounter = meterRegistry.find("bookstore.api.errors").counter();
        if (errorCounter != null) {
            errorMetrics.put("total", errorCounter.count());
        }
        
        businessMetrics.put("errors", errorMetrics);
        
        businessMetrics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(businessMetrics);
    }

    @PostMapping("/test-alert")
    @Operation(summary = "Trigger a test alert for monitoring system validation")
    public ResponseEntity<Map<String, String>> triggerTestAlert(@RequestParam String alertType) {
        Map<String, String> testDetails = Map.of(
            "test", "true",
            "triggeredBy", "admin",
            "timestamp", LocalDateTime.now().toString()
        );
        
        monitoringService.recordCustomMetric("test.alert", "trigger", testDetails);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test alert triggered");
        response.put("alertType", alertType);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system/info")
    @Operation(summary = "Get system information for monitoring")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("processors", runtime.availableProcessors());
        runtimeInfo.put("max_memory", runtime.maxMemory());
        runtimeInfo.put("total_memory", runtime.totalMemory());
        runtimeInfo.put("free_memory", runtime.freeMemory());
        runtimeInfo.put("used_memory", runtime.totalMemory() - runtime.freeMemory());
        
        systemInfo.put("runtime", runtimeInfo);
        
        // System properties
        Map<String, String> systemProps = new HashMap<>();
        systemProps.put("java_version", System.getProperty("java.version"));
        systemProps.put("java_vendor", System.getProperty("java.vendor"));
        systemProps.put("os_name", System.getProperty("os.name"));
        systemProps.put("os_version", System.getProperty("os.version"));
        systemProps.put("os_arch", System.getProperty("os.arch"));
        
        systemInfo.put("system", systemProps);
        systemInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(systemInfo);
    }

    @PostMapping("/metrics/reset")
    @Operation(summary = "Reset specific metrics (for testing purposes)")
    public ResponseEntity<Map<String, String>> resetMetrics(@RequestParam String metricName) {
        // This is primarily for testing - in production you might want to restrict this
        Map<String, String> response = new HashMap<>();
        response.put("message", "Metric reset requested");
        response.put("metric", metricName);
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Note: Micrometer doesn't support resetting counters by design
        // This endpoint is more for documentation and potential future use
        
        return ResponseEntity.ok(response);
    }
}