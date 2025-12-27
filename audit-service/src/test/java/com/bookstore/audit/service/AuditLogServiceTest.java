package com.bookstore.audit.service;

import com.bookstore.audit.dto.AuditLogRequest;
import com.bookstore.audit.dto.AuditLogResponse;
import com.bookstore.audit.dto.AuditLogSearchRequest;
import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLogRequest testRequest;
    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        testRequest = AuditLogRequest.builder()
                .userId(1L)
                .username("testuser")
                .action("CREATE")
                .resourceType("BOOK")
                .resourceId("123")
                .serviceName("book-catalog-service")
                .description("Created a new book")
                .ipAddress("192.168.1.1")
                .correlationId("corr-123")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .severity("INFO")
                .success(true)
                .build();

        testAuditLog = AuditLog.builder()
                .id("audit-1")
                .userId(1L)
                .username("testuser")
                .action("CREATE")
                .resourceType("BOOK")
                .resourceId("123")
                .serviceName("book-catalog-service")
                .description("Created a new book")
                .ipAddress("192.168.1.1")
                .correlationId("corr-123")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .severity("INFO")
                .success(true)
                .build();
    }

    @Test
    void createAuditLog_ShouldCreateAndReturnAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogResponse response = auditLogService.createAuditLog(testRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getAction()).isEqualTo("CREATE");
        assertThat(response.getResourceType()).isEqualTo("BOOK");
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void createAuditLog_ShouldSetDefaultSeverityAndSuccess() {
        // Arrange
        testRequest.setSeverity(null);
        testRequest.setSuccess(null);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        AuditLogResponse response = auditLogService.createAuditLog(testRequest);

        // Assert
        verify(auditLogRepository).save(argThat(log ->
                "INFO".equals(log.getSeverity()) && Boolean.TRUE.equals(log.getSuccess())
        ));
    }

    @Test
    void getAuditLogsByUserId_ShouldReturnPagedResults() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(testAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        when(auditLogRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditLogService.getAuditLogsByUserId(1L, 0, 20);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
        verify(auditLogRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getAuditLogsByCorrelationId_ShouldReturnAllMatchingLogs() {
        // Arrange
        List<AuditLog> logs = Arrays.asList(testAuditLog);
        when(auditLogRepository.findByCorrelationId("corr-123")).thenReturn(logs);

        // Act
        List<AuditLogResponse> result = auditLogService.getAuditLogsByCorrelationId("corr-123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCorrelationId()).isEqualTo("corr-123");
        verify(auditLogRepository, times(1)).findByCorrelationId("corr-123");
    }

    @Test
    void searchAuditLogs_WithUserId_ShouldReturnFilteredResults() {
        // Arrange
        AuditLogSearchRequest searchRequest = AuditLogSearchRequest.builder()
                .userId(1L)
                .page(0)
                .size(20)
                .sortBy("timestamp")
                .sortDirection("DESC")
                .build();

        List<AuditLog> logs = Arrays.asList(testAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        when(auditLogRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditLogService.searchAuditLogs(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void searchAuditLogs_WithDateRange_ShouldReturnFilteredResults() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        AuditLogSearchRequest searchRequest = AuditLogSearchRequest.builder()
                .startDate(start)
                .endDate(end)
                .page(0)
                .size(20)
                .sortBy("timestamp")
                .sortDirection("DESC")
                .build();

        List<AuditLog> logs = Arrays.asList(testAuditLog);
        Page<AuditLog> page = new PageImpl<>(logs);
        when(auditLogRepository.findByTimestampBetween(eq(start), eq(end), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<AuditLogResponse> result = auditLogService.searchAuditLogs(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository, times(1))
                .findByTimestampBetween(eq(start), eq(end), any(Pageable.class));
    }
}
