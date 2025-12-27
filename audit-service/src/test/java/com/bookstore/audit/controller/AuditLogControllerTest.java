package com.bookstore.audit.controller;

import com.bookstore.audit.dto.AuditLogRequest;
import com.bookstore.audit.dto.AuditLogResponse;
import com.bookstore.audit.dto.AuditLogSearchRequest;
import com.bookstore.audit.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditLogService auditLogService;

    private AuditLogRequest testRequest;
    private AuditLogResponse testResponse;

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
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .severity("INFO")
                .success(true)
                .build();

        testResponse = AuditLogResponse.builder()
                .id("audit-1")
                .userId(1L)
                .username("testuser")
                .action("CREATE")
                .resourceType("BOOK")
                .resourceId("123")
                .serviceName("book-catalog-service")
                .description("Created a new book")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .severity("INFO")
                .success(true)
                .build();
    }

    @Test
    void createAuditLog_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        when(auditLogService.createAuditLog(any(AuditLogRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("audit-1"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.action").value("CREATE"))
                .andExpect(jsonPath("$.resourceType").value("BOOK"));
    }

    @Test
    void createAuditLog_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AuditLogRequest invalidRequest = AuditLogRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchAuditLogs_ShouldReturnPagedResults() throws Exception {
        // Arrange
        List<AuditLogResponse> logs = Arrays.asList(testResponse);
        Page<AuditLogResponse> page = new PageImpl<>(logs);
        when(auditLogService.searchAuditLogs(any(AuditLogSearchRequest.class))).thenReturn(page);

        AuditLogSearchRequest searchRequest = AuditLogSearchRequest.builder()
                .userId(1L)
                .page(0)
                .size(20)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/audit/logs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("audit-1"));
    }

    @Test
    void getAuditLogsByUserId_ShouldReturnUserLogs() throws Exception {
        // Arrange
        List<AuditLogResponse> logs = Arrays.asList(testResponse);
        Page<AuditLogResponse> page = new PageImpl<>(logs);
        when(auditLogService.getAuditLogsByUserId(eq(1L), eq(0), eq(20))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/audit/logs/user/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(1));
    }

    @Test
    void getAuditLogsByCorrelationId_ShouldReturnCorrelatedLogs() throws Exception {
        // Arrange
        List<AuditLogResponse> logs = Arrays.asList(testResponse);
        when(auditLogService.getAuditLogsByCorrelationId("corr-123")).thenReturn(logs);

        // Act & Assert
        mockMvc.perform(get("/api/audit/logs/correlation/corr-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("audit-1"));
    }
}
