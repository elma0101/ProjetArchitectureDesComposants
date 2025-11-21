package com.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for creating database backups and restoring from backups.
 * Supports both SQL dumps and application-level data exports.
 */
@Service
public class BackupRestoreService {

    private static final Logger logger = LoggerFactory.getLogger(BackupRestoreService.class);
    private static final String BACKUP_DIR = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Autowired
    private DataExportImportService dataExportImportService;

    @Autowired
    private DataSource dataSource;

    /**
     * Create a full database backup using pg_dump
     */
    @Async
    public CompletableFuture<String> createDatabaseBackup() {
        try {
            logger.info("Starting database backup");
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupFileName = String.format("database_backup_%s.sql", timestamp);
            Path backupPath = createBackupDirectory().resolve(backupFileName);

            // Extract database name from URL
            String databaseName = extractDatabaseName(databaseUrl);
            String host = extractHost(databaseUrl);
            String port = extractPort(databaseUrl);

            // Build pg_dump command
            List<String> command = new ArrayList<>();
            command.add("pg_dump");
            command.add("-h");
            command.add(host);
            command.add("-p");
            command.add(port);
            command.add("-U");
            command.add(databaseUsername);
            command.add("-d");
            command.add(databaseName);
            command.add("-f");
            command.add(backupPath.toString());
            command.add("--verbose");
            command.add("--no-password");

            // Set environment variable for password
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", databasePassword);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database backup completed successfully: {}", backupPath.toString());
                return CompletableFuture.completedFuture(backupPath.toString());
            } else {
                String error = readProcessError(process);
                logger.error("Database backup failed with exit code {}: {}", exitCode, error);
                throw new RuntimeException("Database backup failed: " + error);
            }

        } catch (Exception e) {
            logger.error("Error creating database backup", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Create an application-level data backup
     */
    @Async
    public CompletableFuture<String> createDataBackup() {
        try {
            logger.info("Starting application data backup");
            String backupPath = dataExportImportService.exportAllData();
            logger.info("Application data backup completed: {}", backupPath);
            return CompletableFuture.completedFuture(backupPath);
        } catch (Exception e) {
            logger.error("Error creating application data backup", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Create a combined backup (database + application data)
     */
    @Async
    public CompletableFuture<BackupResult> createFullBackup() {
        try {
            logger.info("Starting full backup (database + application data)");
            
            CompletableFuture<String> databaseBackup = createDatabaseBackup();
            CompletableFuture<String> dataBackup = createDataBackup();

            return CompletableFuture.allOf(databaseBackup, dataBackup)
                .thenApply(v -> {
                    try {
                        BackupResult result = new BackupResult();
                        result.setDatabaseBackupPath(databaseBackup.get());
                        result.setDataBackupPath(dataBackup.get());
                        result.setTimestamp(LocalDateTime.now());
                        result.setSuccess(true);
                        
                        logger.info("Full backup completed successfully");
                        return result;
                    } catch (Exception e) {
                        logger.error("Error completing full backup", e);
                        BackupResult result = new BackupResult();
                        result.setSuccess(false);
                        result.setErrorMessage(e.getMessage());
                        return result;
                    }
                });

        } catch (Exception e) {
            logger.error("Error starting full backup", e);
            BackupResult result = new BackupResult();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            return CompletableFuture.completedFuture(result);
        }
    }

    /**
     * Restore database from SQL backup file
     */
    @Async
    public CompletableFuture<Boolean> restoreDatabase(String backupFilePath) {
        try {
            logger.info("Starting database restore from: {}", backupFilePath);
            
            Path backupPath = Paths.get(backupFilePath);
            if (!Files.exists(backupPath)) {
                throw new IllegalArgumentException("Backup file not found: " + backupFilePath);
            }

            // Extract database connection details
            String databaseName = extractDatabaseName(databaseUrl);
            String host = extractHost(databaseUrl);
            String port = extractPort(databaseUrl);

            // Build psql command
            List<String> command = new ArrayList<>();
            command.add("psql");
            command.add("-h");
            command.add(host);
            command.add("-p");
            command.add(port);
            command.add("-U");
            command.add(databaseUsername);
            command.add("-d");
            command.add(databaseName);
            command.add("-f");
            command.add(backupPath.toString());
            command.add("--quiet");

            // Set environment variable for password
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", databasePassword);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database restore completed successfully");
                return CompletableFuture.completedFuture(true);
            } else {
                String error = readProcessError(process);
                logger.error("Database restore failed with exit code {}: {}", exitCode, error);
                throw new RuntimeException("Database restore failed: " + error);
            }

        } catch (Exception e) {
            logger.error("Error restoring database", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Restore application data from export file
     */
    @Async
    public CompletableFuture<Boolean> restoreData(String backupFilePath) {
        try {
            logger.info("Starting application data restore from: {}", backupFilePath);
            dataExportImportService.importAllData(backupFilePath);
            logger.info("Application data restore completed successfully");
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Error restoring application data", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Scheduled backup job - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        logger.info("Starting scheduled backup");
        createFullBackup().thenAccept(result -> {
            if (result.isSuccess()) {
                logger.info("Scheduled backup completed successfully");
                cleanupOldBackups();
            } else {
                logger.error("Scheduled backup failed: {}", result.getErrorMessage());
            }
        });
    }

    /**
     * Clean up old backup files (keep last 30 days)
     */
    public void cleanupOldBackups() {
        try {
            logger.info("Starting backup cleanup");
            Path backupDir = Paths.get(BACKUP_DIR);
            
            if (!Files.exists(backupDir)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            
            Files.list(backupDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                            .isBefore(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } catch (IOException e) {
                        logger.warn("Error checking file modification time: {}", path, e);
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted old backup file: {}", path);
                    } catch (IOException e) {
                        logger.warn("Error deleting old backup file: {}", path, e);
                    }
                });

            logger.info("Backup cleanup completed");
        } catch (Exception e) {
            logger.error("Error during backup cleanup", e);
        }
    }

    /**
     * List available backup files
     */
    public List<BackupInfo> listBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        
        try {
            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                return backups;
            }

            Files.list(backupDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        BackupInfo info = new BackupInfo();
                        info.setFileName(path.getFileName().toString());
                        info.setFilePath(path.toString());
                        info.setFileSize(Files.size(path));
                        info.setCreatedDate(Files.getLastModifiedTime(path).toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                        info.setType(determineBackupType(path.getFileName().toString()));
                        backups.add(info);
                    } catch (IOException e) {
                        logger.warn("Error reading backup file info: {}", path, e);
                    }
                });

        } catch (Exception e) {
            logger.error("Error listing backup files", e);
        }

        return backups;
    }

    private Path createBackupDirectory() throws IOException {
        Path backupDir = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }
        return backupDir;
    }

    private String extractDatabaseName(String url) {
        // Extract database name from JDBC URL: jdbc:postgresql://host:port/database
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private String extractHost(String url) {
        // Extract host from JDBC URL
        String withoutProtocol = url.substring(url.indexOf("://") + 3);
        return withoutProtocol.substring(0, withoutProtocol.indexOf(':'));
    }

    private String extractPort(String url) {
        // Extract port from JDBC URL
        String withoutProtocol = url.substring(url.indexOf("://") + 3);
        String hostPort = withoutProtocol.substring(0, withoutProtocol.indexOf('/'));
        return hostPort.substring(hostPort.indexOf(':') + 1);
    }

    private String readProcessError(Process process) throws IOException {
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        return error.toString();
    }

    private String determineBackupType(String fileName) {
        if (fileName.contains("database_backup") && fileName.endsWith(".sql")) {
            return "DATABASE";
        } else if (fileName.contains("bookstore_export") && fileName.endsWith(".zip")) {
            return "APPLICATION_DATA";
        } else {
            return "UNKNOWN";
        }
    }

    // Inner classes for backup results and info
    public static class BackupResult {
        private boolean success;
        private String databaseBackupPath;
        private String dataBackupPath;
        private LocalDateTime timestamp;
        private String errorMessage;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getDatabaseBackupPath() { return databaseBackupPath; }
        public void setDatabaseBackupPath(String databaseBackupPath) { this.databaseBackupPath = databaseBackupPath; }
        public String getDataBackupPath() { return dataBackupPath; }
        public void setDataBackupPath(String dataBackupPath) { this.dataBackupPath = dataBackupPath; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class BackupInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime createdDate;
        private String type;

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}