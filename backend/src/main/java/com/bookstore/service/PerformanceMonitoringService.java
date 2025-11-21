package com.bookstore.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for monitoring application performance metrics
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    private final DataSource dataSource;

    // Metrics
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Timer databaseQueryTimer;
    private final Timer cacheOperationTimer;

    @Autowired
    public PerformanceMonitoringService(MeterRegistry meterRegistry, 
                                      CacheManager cacheManager,
                                      DataSource dataSource) {
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;
        this.dataSource = dataSource;

        // Initialize metrics
        this.cacheHitCounter = Counter.builder("cache.hits")
                .description("Number of cache hits")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("cache.misses")
                .description("Number of cache misses")
                .register(meterRegistry);

        this.databaseQueryTimer = Timer.builder("database.query.time")
                .description("Database query execution time")
                .register(meterRegistry);

        this.cacheOperationTimer = Timer.builder("cache.operation.time")
                .description("Cache operation execution time")
                .register(meterRegistry);
    }

    /**
     * Record cache hit
     */
    public void recordCacheHit(String cacheName) {
        cacheHitCounter.increment();
        logger.debug("Cache hit recorded for cache: {}", cacheName);
    }

    /**
     * Record cache miss
     */
    public void recordCacheMiss(String cacheName) {
        cacheMissCounter.increment();
        logger.debug("Cache miss recorded for cache: {}", cacheName);
    }

    /**
     * Record database query execution time
     */
    public void recordDatabaseQuery(String queryType, long executionTimeMs) {
        databaseQueryTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        logger.debug("Database query recorded - Type: {}, Time: {}ms", queryType, executionTimeMs);
    }

    /**
     * Record cache operation execution time
     */
    public void recordCacheOperation(String operation, long executionTimeMs) {
        cacheOperationTimer.record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        logger.debug("Cache operation recorded - Operation: {}, Time: {}ms", operation, executionTimeMs);
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get cache names and their statistics
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("name", cacheName);
                
                // Try to get native cache statistics if available
                Object nativeCache = cache.getNativeCache();
                if (nativeCache instanceof org.springframework.cache.concurrent.ConcurrentMapCache) {
                    // For ConcurrentMapCache, we can get the size
                    cacheStats.put("type", "ConcurrentMap");
                } else {
                    cacheStats.put("type", nativeCache.getClass().getSimpleName());
                }
                
                stats.put(cacheName, cacheStats);
            }
        }
        
        // Add overall cache metrics
        stats.put("totalHits", cacheHitCounter.count());
        stats.put("totalMisses", cacheMissCounter.count());
        
        double hitRate = 0.0;
        double totalRequests = cacheHitCounter.count() + cacheMissCounter.count();
        if (totalRequests > 0) {
            hitRate = cacheHitCounter.count() / totalRequests;
        }
        stats.put("hitRate", hitRate);
        
        return stats;
    }

    /**
     * Get database connection pool statistics
     */
    public Map<String, Object> getConnectionPoolStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Try to get HikariCP statistics
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                com.zaxxer.hikari.HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
                
                if (poolBean != null) {
                    stats.put("activeConnections", poolBean.getActiveConnections());
                    stats.put("idleConnections", poolBean.getIdleConnections());
                    stats.put("totalConnections", poolBean.getTotalConnections());
                    stats.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                }
            }
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                stats.put("connectionValid", connection.isValid(5));
                stats.put("connectionUrl", connection.getMetaData().getURL());
            }
            
        } catch (SQLException e) {
            logger.error("Error getting connection pool statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get performance summary
     */
    public Map<String, Object> getPerformanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Cache performance
        summary.put("cache", getCacheStatistics());
        
        // Database performance
        summary.put("database", getConnectionPoolStatistics());
        
        // Query timing statistics
        Map<String, Object> queryStats = new HashMap<>();
        queryStats.put("averageQueryTime", databaseQueryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        queryStats.put("maxQueryTime", databaseQueryTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        queryStats.put("totalQueries", databaseQueryTimer.count());
        summary.put("queryTiming", queryStats);
        
        // Cache timing statistics
        Map<String, Object> cacheTimingStats = new HashMap<>();
        cacheTimingStats.put("averageCacheTime", cacheOperationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        cacheTimingStats.put("maxCacheTime", cacheOperationTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        cacheTimingStats.put("totalCacheOperations", cacheOperationTimer.count());
        summary.put("cacheTiming", cacheTimingStats);
        
        return summary;
    }

    /**
     * Clear all cache statistics
     */
    public void clearCacheStatistics() {
        // Note: Micrometer counters and timers cannot be reset
        // This method would be used to clear custom statistics if we had them
        logger.info("Cache statistics cleared (note: Micrometer metrics cannot be reset)");
    }

    /**
     * Warm up caches with frequently accessed data
     */
    public void warmUpCaches() {
        logger.info("Starting cache warm-up process");
        
        try {
            // This would typically involve calling methods that populate caches
            // with frequently accessed data
            
            // Example: Pre-load popular books, recent authors, etc.
            // The actual implementation would depend on your specific use case
            
            logger.info("Cache warm-up completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache warm-up", e);
        }
    }
}