package com.bookstore.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest {

    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String serviceName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String correlationId;
    private String severity;
    private Boolean success;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
    @Builder.Default
    private String sortBy = "timestamp";
    @Builder.Default
    private String sortDirection = "DESC";
}
