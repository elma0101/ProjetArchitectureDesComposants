package com.bookstore.controller;

import com.bookstore.service.PerformanceMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for performance monitoring and cache management
 */
@RestController
@RequestMapping("/api/admin/performance")
@Tag(name = "Performance", description = "Performance monitoring and cache management endpoints")
public class PerformanceController {

    private final PerformanceMonitoringService performanceMonitoringService;
    private final CacheManager cacheManager;

    @Autowired
    public PerformanceController(PerformanceMonitoringService performanceMonitoringService,
                               CacheManager cacheManager) {
        this.performanceMonitoringService = performanceMonitoringService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get performance summary", 
               description = "Get comprehensive performance metrics including cache and database statistics")
    @ApiResponse(responseCode = "200", description = "Performance summary retrieved successfully")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        Map<String, Object> summary = performanceMonitoringService.getPerformanceSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "Get cache statistics", 
               description = "Get detailed cache performance statistics")
    @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> stats = performanceMonitoringService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/database/stats")
    @Operation(summary = "Get database statistics", 
               description = "Get database connection pool and performance statistics")
    @ApiResponse(responseCode = "200", description = "Database statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getDatabaseStatistics() {
        Map<String, Object> stats = performanceMonitoringService.getConnectionPoolStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "Clear all caches", 
               description = "Clear all application caches to force fresh data loading")
    @ApiResponse(responseCode = "200", description = "All caches cleared successfully")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        response.put("clearedCaches", String.join(", ", cacheManager.getCacheNames()));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/clear/{cacheName}")
    @Operation(summary = "Clear specific cache", 
               description = "Clear a specific cache by name")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @ApiResponse(responseCode = "404", description = "Cache not found")
    public ResponseEntity<Map<String, String>> clearSpecificCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        Map<String, String> response = new HashMap<>();
        
        if (cache != null) {
            cache.clear();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cache/names")
    @Operation(summary = "Get cache names", 
               description = "Get list of all configured cache names")
    @ApiResponse(responseCode = "200", description = "Cache names retrieved successfully")
    public ResponseEntity<Map<String, Object>> getCacheNames() {
        Map<String, Object> response = new HashMap<>();
        response.put("cacheNames", cacheManager.getCacheNames());
        response.put("totalCaches", cacheManager.getCacheNames().size());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/warmup")
    @Operation(summary = "Warm up caches", 
               description = "Pre-load caches with frequently accessed data")
    @ApiResponse(responseCode = "200", description = "Cache warm-up initiated successfully")
    public ResponseEntity<Map<String, String>> warmUpCaches() {
        performanceMonitoringService.warmUpCaches();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache warm-up initiated successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Get performance health check", 
               description = "Get basic performance health indicators")
    @ApiResponse(responseCode = "200", description = "Performance health check completed")
    public ResponseEntity<Map<String, Object>> getPerformanceHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Cache health
        Map<String, Object> cacheHealth = new HashMap<>();
        cacheHealth.put("configured", !cacheManager.getCacheNames().isEmpty());
        cacheHealth.put("cacheCount", cacheManager.getCacheNames().size());
        health.put("cache", cacheHealth);
        
        // Database health
        Map<String, Object> dbStats = performanceMonitoringService.getConnectionPoolStatistics();
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("connectionValid", dbStats.getOrDefault("connectionValid", false));
        dbHealth.put("hasActiveConnections", 
            dbStats.containsKey("activeConnections") && 
            (Integer) dbStats.getOrDefault("activeConnections", 0) > 0);
        health.put("database", dbHealth);
        
        // Overall health
        boolean isHealthy = (boolean) cacheHealth.get("configured") && 
                           (boolean) dbHealth.get("connectionValid");
        health.put("overall", isHealthy ? "healthy" : "degraded");
        
        return ResponseEntity.ok(health);
    }
}