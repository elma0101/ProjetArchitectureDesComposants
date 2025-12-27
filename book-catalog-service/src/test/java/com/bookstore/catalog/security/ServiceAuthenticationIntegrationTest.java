package com.bookstore.catalog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceAuthenticationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private static final String VALID_API_KEY = "bookstore-internal-service-key-change-in-production";
    
    @Test
    void shouldAllowHealthCheckWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
    
    @Test
    void shouldAllowValidServiceAuthentication() throws Exception {
        mockMvc.perform(get("/api/books")
                .header("X-Service-API-Key", VALID_API_KEY)
                .header("X-Service-Name", "api-gateway"))
            .andExpect(status().isOk());
    }
    
    @Test
    void shouldRejectInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/books")
                .header("X-Service-API-Key", "invalid-key")
                .header("X-Service-Name", "api-gateway"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldRejectUnauthorizedService() throws Exception {
        mockMvc.perform(get("/api/books")
                .header("X-Service-API-Key", VALID_API_KEY)
                .header("X-Service-Name", "unauthorized-service"))
            .andExpect(status().isUnauthorized());
    }
}
