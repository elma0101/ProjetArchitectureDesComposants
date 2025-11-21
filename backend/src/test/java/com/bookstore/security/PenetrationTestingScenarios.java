package com.bookstore.security;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class PenetrationTestingScenarios {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", passwordEncoder.encode("password"), 
                          "test@test.com", Set.of(Role.USER));
        userRepository.save(testUser);
    }
    
    @Test
    void testSqlInjectionAttempts() throws Exception {
        // Test SQL injection in login
        String sqlInjectionPayload = objectMapper.writeValueAsString(
            new LoginRequest("admin'; DROP TABLE users; --", "password")
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sqlInjectionPayload))
                .andExpect(status().isBadRequest());
        
        // Test SQL injection in search parameters
        mockMvc.perform(get("/api/books")
                .param("title", "'; DROP TABLE books; --")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(testUser)))
                .andExpect(status().isOk()); // Should not cause SQL injection
    }
    
    @Test
    void testXssAttempts() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        
        // Test XSS in request parameters
        mockMvc.perform(get("/api/books")
                .param("title", xssPayload)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                    org.hamcrest.Matchers.containsString("<script>"))));
    }
    
    @Test
    void testUnauthorizedAccessAttempts() throws Exception {
        // Test accessing admin endpoints without proper role
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/admin/audit-logs")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(testUser)))
                .andExpect(status().isForbidden());
        
        // Test accessing protected endpoints without token
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testJwtTokenManipulation() throws Exception {
        String validToken = jwtUtil.generateToken(testUser);
        
        // Test with manipulated token
        String manipulatedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";
        
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + manipulatedToken))
                .andExpect(status().isUnauthorized());
        
        // Test with expired token (would need to create an expired token)
        // This is a placeholder for expired token testing
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer expired.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testCsrfProtection() throws Exception {
        // CSRF protection should be disabled for stateless JWT authentication
        // But we test that state-changing operations require proper authentication
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Book\"}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("Referrer-Policy"));
    }
    
    @Test
    void testPasswordBruteForceProtection() throws Exception {
        // Test multiple failed login attempts
        String wrongPasswordRequest = objectMapper.writeValueAsString(
            new LoginRequest("testuser", "wrongpassword")
        );
        
        // Make multiple failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(wrongPasswordRequest))
                    .andExpect(status().isBadRequest());
        }
        
        // Account should still be accessible with correct credentials
        String correctPasswordRequest = objectMapper.writeValueAsString(
            new LoginRequest("testuser", "password")
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(correctPasswordRequest))
                .andExpect(status().isOk());
    }
    
    @Test
    void testPrivilegeEscalation() throws Exception {
        String userToken = jwtUtil.generateToken(testUser);
        
        // Test that regular user cannot perform admin operations
        mockMvc.perform(delete("/api/books/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        
        // Test that regular user cannot access admin endpoints
        mockMvc.perform(get("/api/admin/audit-logs")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testInputValidation() throws Exception {
        String userToken = jwtUtil.generateToken(testUser);
        
        // Test with malformed JSON
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
        
        // Test with oversized input
        String oversizedTitle = "A".repeat(10000);
        String oversizedRequest = objectMapper.writeValueAsString(
            new TestBookRequest(oversizedTitle, "123456789", "Description")
        );
        
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(oversizedRequest))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testDirectoryTraversalAttempts() throws Exception {
        // Test directory traversal in file-related endpoints (if any)
        mockMvc.perform(get("/api/books/../../../etc/passwd"))
                .andExpect(status().isNotFound());
        
        mockMvc.perform(get("/api/books/..%2F..%2F..%2Fetc%2Fpasswd"))
                .andExpect(status().isNotFound());
    }
    
    // Helper classes
    public static class LoginRequest {
        private String username;
        private String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class TestBookRequest {
        private String title;
        private String isbn;
        private String description;
        
        public TestBookRequest(String title, String isbn, String description) {
            this.title = title;
            this.isbn = isbn;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}