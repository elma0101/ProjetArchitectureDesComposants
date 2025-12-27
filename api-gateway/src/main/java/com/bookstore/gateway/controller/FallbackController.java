package com.bookstore.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/user-service")
    @PostMapping("/user-service")
    @PutMapping("/user-service")
    @DeleteMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        logger.warn("User Management Service fallback triggered");
        return createFallbackResponse(
            "User Management Service is currently unavailable. Please try again later.",
            "USER_SERVICE_UNAVAILABLE",
            "Authentication and user management operations are temporarily unavailable. " +
            "If you were trying to log in, please wait a moment and try again."
        );
    }

    @GetMapping("/book-service")
    @PostMapping("/book-service")
    @PutMapping("/book-service")
    @DeleteMapping("/book-service")
    public ResponseEntity<Map<String, Object>> bookServiceFallback() {
        logger.warn("Book Catalog Service fallback triggered");
        return createFallbackResponse(
            "Book Catalog Service is currently unavailable. Please try again later.",
            "BOOK_SERVICE_UNAVAILABLE",
            "Book catalog operations are temporarily unavailable."
        );
    }

    @GetMapping("/loan-service")
    @PostMapping("/loan-service")
    @PutMapping("/loan-service")
    @DeleteMapping("/loan-service")
    public ResponseEntity<Map<String, Object>> loanServiceFallback() {
        logger.warn("Loan Management Service fallback triggered");
        return createFallbackResponse(
            "Loan Management Service is currently unavailable. Please try again later.",
            "LOAN_SERVICE_UNAVAILABLE",
            "Loan operations are temporarily unavailable."
        );
    }

    @GetMapping("/recommendation-service")
    @PostMapping("/recommendation-service")
    public ResponseEntity<Map<String, Object>> recommendationServiceFallback() {
        logger.warn("Recommendation Service fallback triggered");
        return createFallbackResponse(
            "Recommendation Service is currently unavailable. Please try again later.",
            "RECOMMENDATION_SERVICE_UNAVAILABLE",
            "Recommendations are temporarily unavailable."
        );
    }

    @GetMapping("/audit-service")
    @PostMapping("/audit-service")
    public ResponseEntity<Map<String, Object>> auditServiceFallback() {
        logger.warn("Audit Service fallback triggered");
        return createFallbackResponse(
            "Audit Service is currently unavailable. Please try again later.",
            "AUDIT_SERVICE_UNAVAILABLE",
            "Audit logging is temporarily unavailable."
        );
    }

    @GetMapping("/notification-service")
    @PostMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        logger.warn("Notification Service fallback triggered");
        return createFallbackResponse(
            "Notification Service is currently unavailable. Please try again later.",
            "NOTIFICATION_SERVICE_UNAVAILABLE",
            "Notifications are temporarily unavailable."
        );
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message, String errorCode, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("details", details);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("retryAfter", "30 seconds");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}