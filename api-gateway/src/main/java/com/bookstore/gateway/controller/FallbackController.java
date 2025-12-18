package com.bookstore.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    @PostMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse("User Management Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/book-service")
    @PostMapping("/book-service")
    public ResponseEntity<Map<String, Object>> bookServiceFallback() {
        return createFallbackResponse("Book Catalog Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/loan-service")
    @PostMapping("/loan-service")
    public ResponseEntity<Map<String, Object>> loanServiceFallback() {
        return createFallbackResponse("Loan Management Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/recommendation-service")
    @PostMapping("/recommendation-service")
    public ResponseEntity<Map<String, Object>> recommendationServiceFallback() {
        return createFallbackResponse("Recommendation Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/audit-service")
    @PostMapping("/audit-service")
    public ResponseEntity<Map<String, Object>> auditServiceFallback() {
        return createFallbackResponse("Audit Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/notification-service")
    @PostMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createFallbackResponse("Notification Service is currently unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}