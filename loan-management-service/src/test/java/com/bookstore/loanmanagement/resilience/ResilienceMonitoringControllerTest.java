package com.bookstore.loanmanagement.resilience;

import com.bookstore.loanmanagement.controller.ResilienceMonitoringController;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Resilience Monitoring Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResilienceMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void testGetAllCircuitBreakers() throws Exception {
        mockMvc.perform(get("/api/resilience/circuit-breakers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void testGetSpecificCircuitBreaker() throws Exception {
        mockMvc.perform(get("/api/resilience/circuit-breakers/bookCatalogService")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bookCatalogService"))
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.failureRate").exists());
    }

    @Test
    void testGetNonExistentCircuitBreaker() throws Exception {
        mockMvc.perform(get("/api/resilience/circuit-breakers/nonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testResetCircuitBreaker() throws Exception {
        mockMvc.perform(post("/api/resilience/circuit-breakers/bookCatalogService/reset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Circuit breaker reset successfully"))
                .andExpect(jsonPath("$.name").value("bookCatalogService"));
    }

    @Test
    void testTransitionToClosedState() throws Exception {
        mockMvc.perform(post("/api/resilience/circuit-breakers/bookCatalogService/transition-to-closed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Circuit breaker transitioned to CLOSED"))
                .andExpect(jsonPath("$.name").value("bookCatalogService"));
    }

    @Test
    void testGetRetries() throws Exception {
        mockMvc.perform(get("/api/resilience/retries")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void testGetRateLimiters() throws Exception {
        mockMvc.perform(get("/api/resilience/rate-limiters")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    void testGetResilienceHealth() throws Exception {
        mockMvc.perform(get("/api/resilience/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.circuitBreakers").exists())
                .andExpect(jsonPath("$.status").exists());
    }
}
