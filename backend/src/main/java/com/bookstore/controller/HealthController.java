package com.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health and monitoring endpoints")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @GetMapping
    @Operation(
        summary = "Health check endpoint",
        description = "Returns the current health status of the Bookstore API application"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Healthy response",
                    value = """
                        {
                          "status": "UP",
                          "message": "Bookstore API is running",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Bookstore API is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detailed")
    @Operation(
        summary = "Detailed health check",
        description = "Returns detailed health information including database and cache status"
    )
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        // Check database connectivity
        health.put("database", checkDatabase());
        
        // Check Redis connectivity if available
        if (redisConnectionFactory != null) {
            health.put("redis", checkRedis());
        }
        
        // Check application readiness
        health.put("readiness", checkReadiness());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/liveness")
    @Operation(
        summary = "Liveness probe",
        description = "Kubernetes liveness probe endpoint"
    )
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(status);
    }

    @GetMapping("/readiness")
    @Operation(
        summary = "Readiness probe",
        description = "Kubernetes readiness probe endpoint"
    )
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        
        boolean databaseReady = checkDatabase().get("status").equals("UP");
        boolean redisReady = redisConnectionFactory == null || checkRedis().get("status").equals("UP");
        
        if (databaseReady && redisReady) {
            readiness.put("status", "UP");
            readiness.put("message", "Application is ready to serve requests");
        } else {
            readiness.put("status", "DOWN");
            readiness.put("message", "Application is not ready");
        }
        
        readiness.put("database", databaseReady ? "UP" : "DOWN");
        readiness.put("redis", redisReady ? "UP" : "DOWN");
        readiness.put("timestamp", LocalDateTime.now());
        
        return readiness.get("status").equals("UP") 
            ? ResponseEntity.ok(readiness)
            : ResponseEntity.status(503).body(readiness);
    }

    private Map<String, String> checkDatabase() {
        Map<String, String> dbHealth = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                dbHealth.put("status", "UP");
                dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("error", "Database connection is not valid");
            }
        } catch (SQLException e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        return dbHealth;
    }

    private Map<String, String> checkRedis() {
        Map<String, String> redisHealth = new HashMap<>();
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            connection.ping();
            connection.close();
            redisHealth.put("status", "UP");
        } catch (Exception e) {
            redisHealth.put("status", "DOWN");
            redisHealth.put("error", e.getMessage());
        }
        return redisHealth;
    }

    private Map<String, String> checkReadiness() {
        Map<String, String> readiness = new HashMap<>();
        // Add any application-specific readiness checks here
        readiness.put("status", "UP");
        readiness.put("message", "Application components are ready");
        return readiness;
    }
}