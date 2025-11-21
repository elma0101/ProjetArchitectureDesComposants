package com.bookstore.controller;

import com.bookstore.entity.AuditLog;
import com.bookstore.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs", description = "Retrieve audit logs with pagination")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/audit-logs/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by user", description = "Retrieve audit logs for a specific user")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(@PathVariable String username, Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAuditLogsByUsername(username, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/audit-logs/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by action", description = "Retrieve audit logs for a specific action")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(@PathVariable String action, Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/audit-logs/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs by date range", description = "Retrieve audit logs within a date range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}