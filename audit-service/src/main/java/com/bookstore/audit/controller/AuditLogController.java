package com.bookstore.audit.controller;

import com.bookstore.audit.dto.AuditLogRequest;
import com.bookstore.audit.dto.AuditLogResponse;
import com.bookstore.audit.dto.AuditLogSearchRequest;
import com.bookstore.audit.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping("/logs")
    public ResponseEntity<AuditLogResponse> createAuditLog(@Valid @RequestBody AuditLogRequest request) {
        log.info("Creating audit log for action: {} on resource: {}", 
                 request.getAction(), request.getResourceType());
        AuditLogResponse response = auditLogService.createAuditLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logs/search")
    public ResponseEntity<Page<AuditLogResponse>> searchAuditLogs(@RequestBody AuditLogSearchRequest searchRequest) {
        log.info("Searching audit logs with criteria: {}", searchRequest);
        Page<AuditLogResponse> results = auditLogService.searchAuditLogs(searchRequest);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching audit logs for user ID: {}", userId);
        Page<AuditLogResponse> logs = auditLogService.getAuditLogsByUserId(userId, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/correlation/{correlationId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByCorrelationId(
            @PathVariable String correlationId) {
        log.info("Fetching audit logs for correlation ID: {}", correlationId);
        List<AuditLogResponse> logs = auditLogService.getAuditLogsByCorrelationId(correlationId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/date-range")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching audit logs between {} and {}", start, end);
        Page<AuditLogResponse> logs = auditLogService.getAuditLogsByDateRange(start, end, page, size);
        return ResponseEntity.ok(logs);
    }
}
