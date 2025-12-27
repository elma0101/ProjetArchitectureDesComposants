package com.bookstore.audit.service;

import com.bookstore.audit.dto.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogMigrationService {

    private final AuditLogService auditLogService;

    /**
     * Migrate audit logs from the monolith database.
     * This method accepts a list of audit log data from the monolith
     * and creates corresponding entries in Elasticsearch.
     */
    public void migrateAuditLogs(List<Map<String, Object>> monolithAuditLogs) {
        log.info("Starting migration of {} audit logs from monolith", monolithAuditLogs.size());
        
        int successCount = 0;
        int failureCount = 0;

        for (Map<String, Object> logData : monolithAuditLogs) {
            try {
                AuditLogRequest request = mapMonolithDataToRequest(logData);
                auditLogService.createAuditLog(request);
                successCount++;
                
                if (successCount % 100 == 0) {
                    log.info("Migrated {} audit logs so far...", successCount);
                }
            } catch (Exception e) {
                log.error("Failed to migrate audit log: {}", logData, e);
                failureCount++;
            }
        }

        log.info("Migration completed. Success: {}, Failures: {}", successCount, failureCount);
    }

    /**
     * Map monolith audit log data to AuditLogRequest.
     * Adjust field mappings based on the actual monolith schema.
     */
    private AuditLogRequest mapMonolithDataToRequest(Map<String, Object> logData) {
        return AuditLogRequest.builder()
                .userId(getLongValue(logData, "user_id"))
                .username(getStringValue(logData, "username"))
                .action(getStringValue(logData, "action"))
                .resourceType(getStringValue(logData, "resource_type"))
                .resourceId(getStringValue(logData, "resource_id"))
                .serviceName("monolith") // Default service name for migrated logs
                .description(getStringValue(logData, "description"))
                .ipAddress(getStringValue(logData, "ip_address"))
                .correlationId(getStringValue(logData, "correlation_id"))
                .timestamp(getTimestampValue(logData, "timestamp"))
                .severity(getStringValue(logData, "severity", "INFO"))
                .success(getBooleanValue(logData, "success", true))
                .errorMessage(getStringValue(logData, "error_message"))
                .build();
    }

    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        return getStringValue(data, key, null);
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private Boolean getBooleanValue(Map<String, Object> data, String key, Boolean defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private LocalDateTime getTimestampValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return LocalDateTime.now();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        // Add more timestamp parsing logic as needed based on monolith format
        return LocalDateTime.now();
    }
}
