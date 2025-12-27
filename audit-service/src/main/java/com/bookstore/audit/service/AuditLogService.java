package com.bookstore.audit.service;

import com.bookstore.audit.dto.AuditLogRequest;
import com.bookstore.audit.dto.AuditLogResponse;
import com.bookstore.audit.dto.AuditLogSearchRequest;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogResponse createAuditLog(AuditLogRequest request) {
        log.debug("Creating audit log for action: {} on resource: {}", 
                  request.getAction(), request.getResourceType());

        AuditLog auditLog = AuditLog.builder()
                .userId(request.getUserId())
                .username(request.getUsername())
                .action(request.getAction())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .ipAddress(request.getIpAddress())
                .correlationId(request.getCorrelationId())
                .timestamp(request.getTimestamp())
                .metadata(request.getMetadata())
                .severity(request.getSeverity() != null ? request.getSeverity() : "INFO")
                .success(request.getSuccess() != null ? request.getSuccess() : true)
                .errorMessage(request.getErrorMessage())
                .build();

        AuditLog savedLog = auditLogRepository.save(auditLog);
        log.info("Audit log created with ID: {}", savedLog.getId());

        return mapToResponse(savedLog);
    }

    public Page<AuditLogResponse> searchAuditLogs(AuditLogSearchRequest searchRequest) {
        log.debug("Searching audit logs with criteria: {}", searchRequest);

        Pageable pageable = createPageable(searchRequest);

        Page<AuditLog> auditLogs;

        if (searchRequest.getCorrelationId() != null) {
            List<AuditLog> logs = auditLogRepository.findByCorrelationId(searchRequest.getCorrelationId());
            Page<AuditLog> page = new PageImpl<>(logs, pageable, logs.size());
            return page.map(this::mapToResponse);
        } else if (searchRequest.getUserId() != null && searchRequest.getStartDate() != null && searchRequest.getEndDate() != null) {
            auditLogs = auditLogRepository.findByUserIdAndTimestampBetween(
                    searchRequest.getUserId(),
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        } else if (searchRequest.getUserId() != null) {
            auditLogs = auditLogRepository.findByUserId(searchRequest.getUserId(), pageable);
        } else if (searchRequest.getUsername() != null) {
            auditLogs = auditLogRepository.findByUsername(searchRequest.getUsername(), pageable);
        } else if (searchRequest.getAction() != null) {
            auditLogs = auditLogRepository.findByAction(searchRequest.getAction(), pageable);
        } else if (searchRequest.getResourceType() != null && searchRequest.getResourceId() != null) {
            auditLogs = auditLogRepository.findByResourceTypeAndResourceId(
                    searchRequest.getResourceType(),
                    searchRequest.getResourceId(),
                    pageable
            );
        } else if (searchRequest.getResourceType() != null) {
            auditLogs = auditLogRepository.findByResourceType(searchRequest.getResourceType(), pageable);
        } else if (searchRequest.getServiceName() != null) {
            auditLogs = auditLogRepository.findByServiceName(searchRequest.getServiceName(), pageable);
        } else if (searchRequest.getStartDate() != null && searchRequest.getEndDate() != null) {
            auditLogs = auditLogRepository.findByTimestampBetween(
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        } else if (searchRequest.getSeverity() != null) {
            auditLogs = auditLogRepository.findBySeverity(searchRequest.getSeverity(), pageable);
        } else if (searchRequest.getSuccess() != null) {
            auditLogs = auditLogRepository.findBySuccess(searchRequest.getSuccess(), pageable);
        } else {
            auditLogs = auditLogRepository.findAll(pageable);
        }

        return auditLogs.map(this::mapToResponse);
    }

    public List<AuditLogResponse> getAuditLogsByCorrelationId(String correlationId) {
        log.debug("Fetching audit logs for correlation ID: {}", correlationId);
        List<AuditLog> logs = auditLogRepository.findByCorrelationId(correlationId);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<AuditLogResponse> getAuditLogsByUserId(Long userId, int page, int size) {
        log.debug("Fetching audit logs for user ID: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findByUserId(userId, pageable);
        return logs.map(this::mapToResponse);
    }

    public Page<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, int page, int size) {
        log.debug("Fetching audit logs between {} and {}", start, end);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findByTimestampBetween(start, end, pageable);
        return logs.map(this::mapToResponse);
    }

    private Pageable createPageable(AuditLogSearchRequest searchRequest) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(direction, searchRequest.getSortBy())
        );
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .serviceName(auditLog.getServiceName())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .correlationId(auditLog.getCorrelationId())
                .timestamp(auditLog.getTimestamp())
                .metadata(auditLog.getMetadata())
                .severity(auditLog.getSeverity())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .build();
    }
}
