package com.bookstore.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String id;
    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String serviceName;
    private String description;
    private String ipAddress;
    private String correlationId;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private String severity;
    private Boolean success;
    private String errorMessage;
}
