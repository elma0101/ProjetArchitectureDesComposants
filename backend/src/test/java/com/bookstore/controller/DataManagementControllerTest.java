package com.bookstore.controller;

import com.bookstore.service.BackupRestoreService;
import com.bookstore.service.DataExportImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataManagementController.class)
class DataManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackupRestoreService backupRestoreService;

    @MockBean
    private DataExportImportService dataExportImportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateDatabaseBackup() throws Exception {
        // Given
        String backupPath = "backups/database_backup_20241117_120000.sql";
        when(backupRestoreService.createDatabaseBackup())
            .thenReturn(CompletableFuture.completedFuture(backupPath));

        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/database")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Database backup created successfully"))
                .andExpect(jsonPath("$.backupPath").value(backupPath));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateDataBackup() throws Exception {
        // Given
        String backupPath = "exports/bookstore_export_20241117_120000.zip";
        when(backupRestoreService.createDataBackup())
            .thenReturn(CompletableFuture.completedFuture(backupPath));

        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/data")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Application data backup created successfully"))
                .andExpect(jsonPath("$.backupPath").value(backupPath));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateFullBackup() throws Exception {
        // Given
        BackupRestoreService.BackupResult result = new BackupRestoreService.BackupResult();
        result.setSuccess(true);
        result.setDatabaseBackupPath("backups/database_backup_20241117_120000.sql");
        result.setDataBackupPath("exports/bookstore_export_20241117_120000.zip");
        result.setTimestamp(LocalDateTime.now());

        when(backupRestoreService.createFullBackup())
            .thenReturn(CompletableFuture.completedFuture(result));

        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/full")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Full backup created successfully"))
                .andExpect(jsonPath("$.databaseBackupPath").value(result.getDatabaseBackupPath()))
                .andExpect(jsonPath("$.dataBackupPath").value(result.getDataBackupPath()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRestoreDatabase() throws Exception {
        // Given
        String backupPath = "backups/database_backup_20241117_120000.sql";
        when(backupRestoreService.restoreDatabase(backupPath))
            .thenReturn(CompletableFuture.completedFuture(true));

        // When & Then
        mockMvc.perform(post("/api/admin/data/restore/database")
                .with(csrf())
                .param("backupFilePath", backupPath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Database restored successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRestoreData() throws Exception {
        // Given
        String backupPath = "exports/bookstore_export_20241117_120000.zip";
        when(backupRestoreService.restoreData(backupPath))
            .thenReturn(CompletableFuture.completedFuture(true));

        // When & Then
        mockMvc.perform(post("/api/admin/data/restore/data")
                .with(csrf())
                .param("backupFilePath", backupPath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Application data restored successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportAllData() throws Exception {
        // Given
        String exportPath = "exports/bookstore_export_20241117_120000.zip";
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("authors", 10L);
        statistics.put("books", 50L);

        when(dataExportImportService.exportAllData()).thenReturn(exportPath);
        when(dataExportImportService.getExportStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(post("/api/admin/data/export/all")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data exported successfully"))
                .andExpect(jsonPath("$.exportPath").value(exportPath))
                .andExpect(jsonPath("$.statistics.authors").value(10))
                .andExpect(jsonPath("$.statistics.books").value(50));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportEntityData() throws Exception {
        // Given
        String entityType = "authors";
        String exportPath = "exports/authors_export_20241117_120000.json";
        when(dataExportImportService.exportEntityData(entityType)).thenReturn(exportPath);

        // When & Then
        mockMvc.perform(post("/api/admin/data/export/{entityType}", entityType)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Entity data exported successfully"))
                .andExpect(jsonPath("$.exportPath").value(exportPath))
                .andExpect(jsonPath("$.entityType").value(entityType));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldImportAllData() throws Exception {
        // Given
        String filePath = "exports/bookstore_export_20241117_120000.zip";

        // When & Then
        mockMvc.perform(post("/api/admin/data/import/all")
                .with(csrf())
                .param("filePath", filePath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Data imported successfully"))
                .andExpect(jsonPath("$.importPath").value(filePath));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldImportEntityData() throws Exception {
        // Given
        String entityType = "authors";
        String filePath = "exports/authors_export_20241117_120000.json";

        // When & Then
        mockMvc.perform(post("/api/admin/data/import/{entityType}", entityType)
                .with(csrf())
                .param("filePath", filePath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Entity data imported successfully"))
                .andExpect(jsonPath("$.entityType").value(entityType))
                .andExpect(jsonPath("$.importPath").value(filePath));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListBackups() throws Exception {
        // Given
        BackupRestoreService.BackupInfo backup1 = new BackupRestoreService.BackupInfo();
        backup1.setFileName("database_backup_20241117_120000.sql");
        backup1.setType("DATABASE");
        backup1.setFileSize(1024L);

        BackupRestoreService.BackupInfo backup2 = new BackupRestoreService.BackupInfo();
        backup2.setFileName("bookstore_export_20241117_120000.zip");
        backup2.setType("APPLICATION_DATA");
        backup2.setFileSize(2048L);

        when(backupRestoreService.listBackups()).thenReturn(Arrays.asList(backup1, backup2));

        // When & Then
        mockMvc.perform(get("/api/admin/data/backups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.backups").isArray())
                .andExpect(jsonPath("$.backups[0].fileName").value("database_backup_20241117_120000.sql"))
                .andExpect(jsonPath("$.backups[0].type").value("DATABASE"))
                .andExpect(jsonPath("$.backups[1].fileName").value("bookstore_export_20241117_120000.zip"))
                .andExpect(jsonPath("$.backups[1].type").value("APPLICATION_DATA"))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetExportStatistics() throws Exception {
        // Given
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("authors", 10L);
        statistics.put("books", 50L);
        statistics.put("users", 5L);
        statistics.put("loans", 25L);

        when(dataExportImportService.getExportStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/admin/data/export/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statistics.authors").value(10))
                .andExpect(jsonPath("$.statistics.books").value(50))
                .andExpect(jsonPath("$.statistics.users").value(5))
                .andExpect(jsonPath("$.statistics.loans").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateExportFile() throws Exception {
        // Given
        String filePath = "exports/bookstore_export_20241117_120000.zip";
        when(dataExportImportService.validateExportFile(filePath)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/admin/data/validate")
                .with(csrf())
                .param("filePath", filePath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.filePath").value(filePath))
                .andExpect(jsonPath("$.message").value("File is valid"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCleanupOldBackups() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/data/cleanup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Old backups cleaned up successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdminUsers() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/database")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/database")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleExportFailure() throws Exception {
        // Given
        when(dataExportImportService.exportAllData()).thenThrow(new RuntimeException("Export failed"));

        // When & Then
        mockMvc.perform(post("/api/admin/data/export/all")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Data export failed: Export failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleBackupFailure() throws Exception {
        // Given
        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Backup failed"));
        when(backupRestoreService.createDatabaseBackup()).thenReturn(failedFuture);

        // When & Then
        mockMvc.perform(post("/api/admin/data/backup/database")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database backup failed: java.lang.RuntimeException: Backup failed"));
    }
}