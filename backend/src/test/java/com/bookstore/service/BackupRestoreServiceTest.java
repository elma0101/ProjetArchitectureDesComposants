package com.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupRestoreServiceTest {

    @Mock
    private DataExportImportService dataExportImportService;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private BackupRestoreService backupRestoreService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(backupRestoreService, "databaseUrl", "jdbc:postgresql://localhost:5432/bookstore");
        ReflectionTestUtils.setField(backupRestoreService, "databaseUsername", "bookstore_user");
        ReflectionTestUtils.setField(backupRestoreService, "databasePassword", "password");
    }

    @Test
    void shouldCreateDataBackup() throws Exception {
        // Given
        String expectedPath = "exports/bookstore_export_20241117_120000.zip";
        when(dataExportImportService.exportAllData()).thenReturn(expectedPath);

        // When
        CompletableFuture<String> result = backupRestoreService.createDataBackup();

        // Then
        assertThat(result.get()).isEqualTo(expectedPath);
        verify(dataExportImportService).exportAllData();
    }

    @Test
    void shouldCreateFullBackup() throws Exception {
        // Given
        String dataBackupPath = "exports/bookstore_export_20241117_120000.zip";
        when(dataExportImportService.exportAllData()).thenReturn(dataBackupPath);

        // When
        CompletableFuture<BackupRestoreService.BackupResult> result = backupRestoreService.createFullBackup();

        // Then
        BackupRestoreService.BackupResult backupResult = result.get();
        assertThat(backupResult.getDataBackupPath()).isEqualTo(dataBackupPath);
        assertThat(backupResult.isSuccess()).isTrue();
        verify(dataExportImportService).exportAllData();
    }

    @Test
    void shouldRestoreData() throws Exception {
        // Given
        String backupPath = "exports/bookstore_export_20241117_120000.zip";
        doNothing().when(dataExportImportService).importAllData(anyString());

        // When
        CompletableFuture<Boolean> result = backupRestoreService.restoreData(backupPath);

        // Then
        assertThat(result.get()).isTrue();
        verify(dataExportImportService).importAllData(backupPath);
    }

    @Test
    void shouldHandleDataBackupFailure() throws Exception {
        // Given
        when(dataExportImportService.exportAllData()).thenThrow(new RuntimeException("Export failed"));

        // When
        CompletableFuture<String> result = backupRestoreService.createDataBackup();

        // Then
        assertThat(result.isCompletedExceptionally()).isTrue();
    }

    @Test
    void shouldHandleDataRestoreFailure() throws Exception {
        // Given
        String backupPath = "exports/bookstore_export_20241117_120000.zip";
        doThrow(new RuntimeException("Import failed")).when(dataExportImportService).importAllData(anyString());

        // When
        CompletableFuture<Boolean> result = backupRestoreService.restoreData(backupPath);

        // Then
        assertThat(result.isCompletedExceptionally()).isTrue();
    }

    @Test
    void shouldListBackups() {
        // When
        List<BackupRestoreService.BackupInfo> backups = backupRestoreService.listBackups();

        // Then
        assertThat(backups).isNotNull();
        // Note: This test will return empty list if no backup directory exists, which is expected
    }

    @Test
    void shouldExtractDatabaseNameFromUrl() {
        // Given
        String url = "jdbc:postgresql://localhost:5432/bookstore";
        
        // When
        String databaseName = ReflectionTestUtils.invokeMethod(backupRestoreService, "extractDatabaseName", url);
        
        // Then
        assertThat(databaseName).isEqualTo("bookstore");
    }

    @Test
    void shouldExtractHostFromUrl() {
        // Given
        String url = "jdbc:postgresql://localhost:5432/bookstore";
        
        // When
        String host = ReflectionTestUtils.invokeMethod(backupRestoreService, "extractHost", url);
        
        // Then
        assertThat(host).isEqualTo("localhost");
    }

    @Test
    void shouldExtractPortFromUrl() {
        // Given
        String url = "jdbc:postgresql://localhost:5432/bookstore";
        
        // When
        String port = ReflectionTestUtils.invokeMethod(backupRestoreService, "extractPort", url);
        
        // Then
        assertThat(port).isEqualTo("5432");
    }

    @Test
    void shouldDetermineBackupType() {
        // When & Then
        String databaseType = ReflectionTestUtils.invokeMethod(backupRestoreService, "determineBackupType", "database_backup_20241117_120000.sql");
        assertThat(databaseType).isEqualTo("DATABASE");

        String dataType = ReflectionTestUtils.invokeMethod(backupRestoreService, "determineBackupType", "bookstore_export_20241117_120000.zip");
        assertThat(dataType).isEqualTo("APPLICATION_DATA");

        String unknownType = ReflectionTestUtils.invokeMethod(backupRestoreService, "determineBackupType", "unknown_file.txt");
        assertThat(unknownType).isEqualTo("UNKNOWN");
    }
}