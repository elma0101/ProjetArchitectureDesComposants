package com.bookstore.controller;

import com.bookstore.service.MonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for MonitoringController
 */
@WebMvcTest(MonitoringController.class)
@Import({com.bookstore.config.TestSecurityConfig.class})
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MonitoringService monitoringService;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private HealthEndpoint healthEndpoint;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDetailedHealth() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMetricsSummary() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBusinessMetrics() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/metrics/business"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.loans").exists())
                .andExpect(jsonPath("$.users").exists())
                .andExpect(jsonPath("$.search").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTriggerTestAlert() throws Exception {
        mockMvc.perform(post("/api/admin/monitoring/test-alert")
                        .param("alertType", "TEST_ALERT"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Test alert triggered"))
                .andExpect(jsonPath("$.alertType").value("TEST_ALERT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSystemInfo() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/system/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.runtime").exists())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.runtime.processors").exists())
                .andExpect(jsonPath("$.runtime.max_memory").exists())
                .andExpect(jsonPath("$.system.java_version").exists())
                .andExpect(jsonPath("$.system.os_name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testResetMetrics() throws Exception {
        mockMvc.perform(post("/api/admin/monitoring/metrics/reset")
                        .param("metricName", "test.metric"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Metric reset requested"))
                .andExpect(jsonPath("$.metric").value("test.metric"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/health/detailed"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/monitoring/health/detailed"))
                .andExpect(status().isUnauthorized());
    }
}