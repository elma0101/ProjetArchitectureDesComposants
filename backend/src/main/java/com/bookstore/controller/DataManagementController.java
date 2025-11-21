package com.bookstore.controller;

import com.bookstore.service.BackupRestoreService;
import com.bookstore.service.DataExportImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for data management operations including backup, restore, export, and import.
 */
@RestController
@RequestMapping("/api/admin/data")
@Tag(name = "Data Management", description = "Data backup, restore, export, and import operations")
@PreAuthorize("hasRole('ADMIN')")
public class DataManagementController {

    private static final Logger logger = LoggerFactory.getLogger(DataManagementController.class);

    @Autowired
    private BackupRestoreService backupRestoreService;

    @Autowired
    private DataExportImportService dataExportImportService;

    @Operation(summary = "Create database backup", description = "Creates a full database backup using pg_dump")
    @PostMapping("/backup/database")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createDatabaseBackup() {
        logger.info("Database backup requested");
        
        return backupRestoreService.createDatabaseBackup()
            .thenApply(backupPath -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Database backup created successfully");
                response.put("backupPath", backupPath);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                logger.error("Database backup failed", throwable);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Database backup failed: " + throwable.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
    }

    @Operation(summary = "Create application data backup", description = "Creates a backup of application data in JSON format")
    @PostMapping("/backup/data")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createDataBackup() {
        logger.info("Application data backup requested");
        
        return backupRestoreService.createDataBackup()
            .thenApply(backupPath -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Application data backup created successfully");
                response.put("backupPath", backupPath);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                logger.error("Application data backup failed", throwable);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application data backup failed: " + throwable.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
    }

    @Operation(summary = "Create full backup", description = "Creates both database and application data backups")
    @PostMapping("/backup/full")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createFullBackup() {
        logger.info("Full backup requested");
        
        return backupRestoreService.createFullBackup()
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                if (result.isSuccess()) {
                    response.put("success", true);
                    response.put("message", "Full backup created successfully");
                    response.put("databaseBackupPath", result.getDatabaseBackupPath());
                    response.put("dataBackupPath", result.getDataBackupPath());
                    response.put("timestamp", result.getTimestamp());
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Full backup failed: " + result.getErrorMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            });
    }

    @Operation(summary = "Restore database", description = "Restores database from a SQL backup file")
    @PostMapping("/restore/database")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> restoreDatabase(
            @Parameter(description = "Path to the backup file") @RequestParam String backupFilePath) {
        logger.info("Database restore requested from: {}", backupFilePath);
        
        return backupRestoreService.restoreDatabase(backupFilePath)
            .thenApply(success -> {
                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "Database restored successfully");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Database restore failed");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            })
            .exceptionally(throwable -> {
                logger.error("Database restore failed", throwable);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Database restore failed: " + throwable.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
    }

    @Operation(summary = "Restore application data", description = "Restores application data from an export file")
    @PostMapping("/restore/data")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> restoreData(
            @Parameter(description = "Path to the export file") @RequestParam String backupFilePath) {
        logger.info("Application data restore requested from: {}", backupFilePath);
        
        return backupRestoreService.restoreData(backupFilePath)
            .thenApply(success -> {
                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "Application data restored successfully");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Application data restore failed");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            })
            .exceptionally(throwable -> {
                logger.error("Application data restore failed", throwable);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Application data restore failed: " + throwable.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            });
    }

    @Operation(summary = "Export all data", description = "Exports all application data to a compressed file")
    @PostMapping("/export/all")
    public ResponseEntity<Map<String, Object>> exportAllData() {
        try {
            logger.info("Data export requested");
            String exportPath = dataExportImportService.exportAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data exported successfully");
            response.put("exportPath", exportPath);
            response.put("statistics", dataExportImportService.getExportStatistics());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Data export failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Data export failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Export entity data", description = "Exports specific entity data to a JSON file")
    @PostMapping("/export/{entityType}")
    public ResponseEntity<Map<String, Object>> exportEntityData(
            @Parameter(description = "Type of entity to export") @PathVariable String entityType) {
        try {
            logger.info("Entity export requested for: {}", entityType);
            String exportPath = dataExportImportService.exportEntityData(entityType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entity data exported successfully");
            response.put("exportPath", exportPath);
            response.put("entityType", entityType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Entity export failed for: {}", entityType, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Entity export failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Import all data", description = "Imports all application data from a compressed file")
    @PostMapping("/import/all")
    public ResponseEntity<Map<String, Object>> importAllData(
            @Parameter(description = "Path to the import file") @RequestParam String filePath) {
        try {
            logger.info("Data import requested from: {}", filePath);
            dataExportImportService.importAllData(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data imported successfully");
            response.put("importPath", filePath);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Data import failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Data import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Import entity data", description = "Imports specific entity data from a JSON file")
    @PostMapping("/import/{entityType}")
    public ResponseEntity<Map<String, Object>> importEntityData(
            @Parameter(description = "Type of entity to import") @PathVariable String entityType,
            @Parameter(description = "Path to the import file") @RequestParam String filePath) {
        try {
            logger.info("Entity import requested for: {} from: {}", entityType, filePath);
            dataExportImportService.importEntityData(entityType, filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entity data imported successfully");
            response.put("entityType", entityType);
            response.put("importPath", filePath);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Entity import failed for: {}", entityType, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Entity import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Upload and import data", description = "Uploads and imports data from a file")
    @PostMapping("/import/upload")
    public ResponseEntity<Map<String, Object>> uploadAndImportData(
            @Parameter(description = "File to upload and import") @RequestParam("file") MultipartFile file) {
        try {
            logger.info("File upload and import requested: {}", file.getOriginalFilename());
            
            // Save uploaded file temporarily
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, "import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile);
            
            // Import the data
            dataExportImportService.importAllData(tempFile.toString());
            
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded and imported successfully");
            response.put("fileName", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("File upload and import failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "File upload and import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "List backups", description = "Lists all available backup files")
    @GetMapping("/backups")
    public ResponseEntity<Map<String, Object>> listBackups() {
        try {
            List<BackupRestoreService.BackupInfo> backups = backupRestoreService.listBackups();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("backups", backups);
            response.put("count", backups.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to list backups", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to list backups: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Get export statistics", description = "Gets statistics about exportable data")
    @GetMapping("/export/statistics")
    public ResponseEntity<Map<String, Object>> getExportStatistics() {
        try {
            Map<String, Long> statistics = dataExportImportService.getExportStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to get export statistics", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get export statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Validate export file", description = "Validates the integrity of an export file")
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateExportFile(
            @Parameter(description = "Path to the file to validate") @RequestParam String filePath) {
        try {
            boolean isValid = dataExportImportService.validateExportFile(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", isValid);
            response.put("filePath", filePath);
            response.put("message", isValid ? "File is valid" : "File is invalid or corrupted");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("File validation failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "File validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Cleanup old backups", description = "Removes old backup files to free up space")
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldBackups() {
        try {
            backupRestoreService.cleanupOldBackups();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Old backups cleaned up successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Backup cleanup failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Backup cleanup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}