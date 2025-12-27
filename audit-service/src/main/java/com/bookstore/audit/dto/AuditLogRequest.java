package com.bookstore.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AuditLogRequest {

    private Long userId;

    private String username;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource type is required")
    private String resourceType;

    private String resourceId;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String description;

    private String ipAddress;

    private String correlationId;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    private Map<String, Object> metadata;

    private String severity;

    private Boolean success;

    private String errorMessage;
}
