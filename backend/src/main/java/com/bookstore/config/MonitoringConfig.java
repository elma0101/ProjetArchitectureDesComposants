package com.bookstore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for monitoring and observability features
 */
@Configuration
public class MonitoringConfig {

    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger totalBookLoans = new AtomicInteger(0);
    private final AtomicInteger failedOperations = new AtomicInteger(0);

    @Bean
    public Counter bookLoanCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bookstore.loans.total")
                .description("Total number of book loans")
                .tag("type", "loan")
                .register(meterRegistry);
    }

    @Bean
    public Counter bookReturnCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bookstore.returns.total")
                .description("Total number of book returns")
                .tag("type", "return")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bookstore.users.registrations")
                .description("Total number of user registrations")
                .tag("type", "registration")
                .register(meterRegistry);
    }

    @Bean
    public Counter apiErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bookstore.api.errors")
                .description("Total number of API errors")
                .tag("type", "error")
                .register(meterRegistry);
    }

    @Bean
    public Timer bookSearchTimer(MeterRegistry meterRegistry) {
        return Timer.builder("bookstore.search.duration")
                .description("Time taken for book searches")
                .tag("operation", "search")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("bookstore.database.operation.duration")
                .description("Time taken for database operations")
                .tag("operation", "database")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("bookstore.users.active", activeUsers, AtomicInteger::get)
                .description("Number of currently active users")
                .register(meterRegistry);
    }

    @Bean
    public Gauge totalLoansGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("bookstore.loans.current", totalBookLoans, AtomicInteger::get)
                .description("Current number of active loans")
                .register(meterRegistry);
    }

    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(1)) {
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connected")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connection invalid")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return () -> {
            try {
                String pong = redisTemplate.getConnectionFactory()
                        .getConnection()
                        .ping();
                if ("PONG".equals(pong)) {
                    return Health.up()
                            .withDetail("redis", "Connected")
                            .withDetail("response", pong)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("redis", "Unexpected response")
                            .withDetail("response", pong)
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("redis", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator customApplicationHealthIndicator() {
        return () -> {
            // Custom business logic health checks
            int failedOps = failedOperations.get();
            if (failedOps > 100) {
                return Health.down()
                        .withDetail("application", "High failure rate")
                        .withDetail("failedOperations", failedOps)
                        .build();
            }
            
            return Health.up()
                    .withDetail("application", "Running normally")
                    .withDetail("activeUsers", activeUsers.get())
                    .withDetail("totalLoans", totalBookLoans.get())
                    .withDetail("failedOperations", failedOps)
                    .build();
        };
    }

    // Utility methods for updating metrics
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    public void incrementTotalLoans() {
        totalBookLoans.incrementAndGet();
    }

    public void decrementTotalLoans() {
        totalBookLoans.decrementAndGet();
    }

    public void incrementFailedOperations() {
        failedOperations.incrementAndGet();
    }
}