package com.bookstore.audit.controller;

import com.bookstore.audit.service.AuditLogMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final AuditLogMigrationService migrationService;

    @PostMapping("/migrate")
    public ResponseEntity<Map<String, String>> migrateAuditLogs(@RequestBody List<Map<String, Object>> auditLogs) {
        log.info("Received request to migrate {} audit logs", auditLogs.size());
        
        try {
            migrationService.migrateAuditLogs(auditLogs);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Audit logs migration completed",
                    "count", String.valueOf(auditLogs.size())
            ));
        } catch (Exception e) {
            log.error("Error during audit log migration", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Migration failed: " + e.getMessage()
            ));
        }
    }
}
