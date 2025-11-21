package com.bookstore.service;

import com.bookstore.config.MonitoringConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for handling application monitoring and alerting
 */
@Service
public class MonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    private static final Logger alertLogger = LoggerFactory.getLogger("ALERT");

    private final Counter bookLoanCounter;
    private final Counter bookReturnCounter;
    private final Counter userRegistrationCounter;
    private final Counter apiErrorCounter;
    private final Timer bookSearchTimer;
    private final Timer databaseOperationTimer;
    private final MonitoringConfig monitoringConfig;
    private final MeterRegistry meterRegistry;

    // Alert thresholds
    private static final int ERROR_RATE_THRESHOLD = 10; // errors per minute
    private static final int RESPONSE_TIME_THRESHOLD = 5000; // 5 seconds
    private static final int ACTIVE_USERS_THRESHOLD = 1000;

    // Tracking for alerting
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAlertTimes = new ConcurrentHashMap<>();

    @Autowired
    public MonitoringService(Counter bookLoanCounter, 
                           Counter bookReturnCounter,
                           Counter userRegistrationCounter,
                           Counter apiErrorCounter,
                           Timer bookSearchTimer,
                           Timer databaseOperationTimer,
                           MonitoringConfig monitoringConfig,
                           MeterRegistry meterRegistry) {
        this.bookLoanCounter = bookLoanCounter;
        this.bookReturnCounter = bookReturnCounter;
        this.userRegistrationCounter = userRegistrationCounter;
        this.apiErrorCounter = apiErrorCounter;
        this.bookSearchTimer = bookSearchTimer;
        this.databaseOperationTimer = databaseOperationTimer;
        this.monitoringConfig = monitoringConfig;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record a book loan event
     */
    public void recordBookLoan(String userId, String bookId) {
        bookLoanCounter.increment();
        monitoringConfig.incrementTotalLoans();
        
        MDC.put("userId", userId);
        MDC.put("bookId", bookId);
        MDC.put("operation", "loan");
        logger.info("Book loan recorded: user={}, book={}", userId, bookId);
        MDC.clear();
    }

    /**
     * Record a book return event
     */
    public void recordBookReturn(String userId, String bookId) {
        bookReturnCounter.increment();
        monitoringConfig.decrementTotalLoans();
        
        MDC.put("userId", userId);
        MDC.put("bookId", bookId);
        MDC.put("operation", "return");
        logger.info("Book return recorded: user={}, book={}", userId, bookId);
        MDC.clear();
    }

    /**
     * Record a user registration event
     */
    public void recordUserRegistration(String userId, String email) {
        userRegistrationCounter.increment();
        
        MDC.put("userId", userId);
        MDC.put("email", email);
        MDC.put("operation", "registration");
        logger.info("User registration recorded: user={}, email={}", userId, email);
        MDC.clear();
    }

    /**
     * Record user login/logout for active user tracking
     */
    public void recordUserLogin(String userId) {
        monitoringConfig.incrementActiveUsers();
        
        MDC.put("userId", userId);
        MDC.put("operation", "login");
        logger.info("User login recorded: user={}", userId);
        MDC.clear();
    }

    public void recordUserLogout(String userId) {
        monitoringConfig.decrementActiveUsers();
        
        MDC.put("userId", userId);
        MDC.put("operation", "logout");
        logger.info("User logout recorded: user={}", userId);
        MDC.clear();
    }

    /**
     * Record API error and check for alerting thresholds
     */
    public void recordApiError(String endpoint, String errorType, String errorMessage, Exception exception) {
        apiErrorCounter.increment();
        monitoringConfig.incrementFailedOperations();
        
        MDC.put("endpoint", endpoint);
        MDC.put("errorType", errorType);
        MDC.put("operation", "error");
        logger.error("API error recorded: endpoint={}, type={}, message={}", 
                    endpoint, errorType, errorMessage, exception);
        
        // Check for error rate threshold
        String errorKey = endpoint + ":" + errorType;
        AtomicInteger count = errorCounts.computeIfAbsent(errorKey, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        if (currentCount >= ERROR_RATE_THRESHOLD) {
            triggerAlert("HIGH_ERROR_RATE", 
                        String.format("High error rate detected for %s: %d errors", endpoint, currentCount),
                        Map.of("endpoint", endpoint, "errorType", errorType, "count", String.valueOf(currentCount)));
            count.set(0); // Reset counter after alert
        }
        
        MDC.clear();
    }

    /**
     * Record search operation timing
     */
    public Timer.Sample startSearchTimer() {
        return Timer.start();
    }

    public void recordSearchTime(Timer.Sample sample, String searchType, int resultCount) {
        long duration = sample.stop(bookSearchTimer);
        
        MDC.put("searchType", searchType);
        MDC.put("resultCount", String.valueOf(resultCount));
        MDC.put("duration", String.valueOf(duration));
        MDC.put("operation", "search");
        logger.info("Search completed: type={}, results={}, duration={}ms", 
                   searchType, resultCount, duration / 1_000_000);
        
        // Check for slow response time
        if (duration > RESPONSE_TIME_THRESHOLD * 1_000_000) { // Convert to nanoseconds
            triggerAlert("SLOW_RESPONSE", 
                        String.format("Slow search response detected: %dms for %s", 
                                    duration / 1_000_000, searchType),
                        Map.of("searchType", searchType, "duration", String.valueOf(duration / 1_000_000)));
        }
        
        MDC.clear();
    }

    /**
     * Record database operation timing
     */
    public Timer.Sample startDatabaseTimer() {
        return Timer.start();
    }

    public void recordDatabaseTime(Timer.Sample sample, String operation, String table) {
        long duration = sample.stop(databaseOperationTimer);
        
        MDC.put("dbOperation", operation);
        MDC.put("table", table);
        MDC.put("duration", String.valueOf(duration));
        MDC.put("operation", "database");
        logger.debug("Database operation completed: operation={}, table={}, duration={}ms", 
                    operation, table, duration / 1_000_000);
        
        // Check for slow database operations
        if (duration > RESPONSE_TIME_THRESHOLD * 1_000_000) {
            triggerAlert("SLOW_DATABASE", 
                        String.format("Slow database operation detected: %dms for %s on %s", 
                                    duration / 1_000_000, operation, table),
                        Map.of("operation", operation, "table", table, "duration", String.valueOf(duration / 1_000_000)));
        }
        
        MDC.clear();
    }

    /**
     * Trigger an alert with rate limiting
     */
    private void triggerAlert(String alertType, String message, Map<String, String> details) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAlert = lastAlertTimes.get(alertType);
        
        // Rate limit alerts - only send if last alert was more than 5 minutes ago
        if (lastAlert == null || now.minusMinutes(5).isAfter(lastAlert)) {
            lastAlertTimes.put(alertType, now);
            
            MDC.put("alertType", alertType);
            MDC.put("severity", getSeverity(alertType));
            details.forEach(MDC::put);
            
            alertLogger.error("ALERT TRIGGERED: {} - {}", alertType, message);
            
            // In a real implementation, you would send this to your alerting system
            // (e.g., PagerDuty, Slack, email, etc.)
            sendToAlertingSystem(alertType, message, details);
            
            MDC.clear();
        }
    }

    /**
     * Get severity level for alert type
     */
    private String getSeverity(String alertType) {
        return switch (alertType) {
            case "HIGH_ERROR_RATE", "SLOW_DATABASE" -> "CRITICAL";
            case "SLOW_RESPONSE" -> "WARNING";
            default -> "INFO";
        };
    }

    /**
     * Send alert to external alerting system
     * In a real implementation, this would integrate with your alerting infrastructure
     */
    private void sendToAlertingSystem(String alertType, String message, Map<String, String> details) {
        // Placeholder for external alerting system integration
        logger.warn("Alert would be sent to external system: type={}, message={}, details={}", 
                   alertType, message, details);
        
        // Example integrations:
        // - Send to Slack webhook
        // - Send to PagerDuty API
        // - Send email notification
        // - Send to monitoring dashboard
    }

    /**
     * Record custom business metric
     */
    public void recordCustomMetric(String metricName, String operation, Map<String, String> tags) {
        tags.forEach(MDC::put);
        MDC.put("metricName", metricName);
        MDC.put("operation", operation);
        
        logger.info("Custom metric recorded: metric={}, operation={}, tags={}", 
                   metricName, operation, tags);
        
        MDC.clear();
    }

    /**
     * Get the meter registry for custom metric creation
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}