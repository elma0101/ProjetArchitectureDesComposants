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
public class SecurityIntegrationTest {
    
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
    
    private User adminUser;
    private User librarianUser;
    private User regularUser;
    
    @BeforeEach
    void setUp() {
        // Create test users
        adminUser = new User("admin", passwordEncoder.encode("password"), 
                           "admin@test.com", Set.of(Role.ADMIN));
        librarianUser = new User("librarian", passwordEncoder.encode("password"), 
                               "librarian@test.com", Set.of(Role.LIBRARIAN));
        regularUser = new User("user", passwordEncoder.encode("password"), 
                             "user@test.com", Set.of(Role.USER));
        
        userRepository.save(adminUser);
        userRepository.save(librarianUser);
        userRepository.save(regularUser);
    }
    
    @Test
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed()); // GET not allowed, POST is
        
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testSuccessfulAuthentication() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("admin", "password")
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles").isArray());
    }
    
    @Test
    void testInvalidCredentials() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            new LoginRequest("admin", "wrongpassword")
        );
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }
    
    @Test
    void testRoleBasedAccess() throws Exception {
        String adminToken = jwtUtil.generateToken(adminUser);
        String librarianToken = jwtUtil.generateToken(librarianUser);
        String userToken = jwtUtil.generateToken(regularUser);
        
        // Admin can access admin endpoints
        mockMvc.perform(get("/api/admin/audit-logs")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        
        // Librarian cannot access admin endpoints
        mockMvc.perform(get("/api/admin/audit-logs")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isForbidden());
        
        // Regular user cannot access admin endpoints
        mockMvc.perform(get("/api/admin/audit-logs")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        
        // All authenticated users can read books
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
        
        // Only librarians and admins can create books
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request due to validation, but authorized
    }
    
    @Test
    void testJwtTokenValidation() throws Exception {
        String validToken = jwtUtil.generateToken(adminUser);
        String invalidToken = "invalid.jwt.token";
        
        // Valid token should work
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
        
        // Invalid token should be rejected
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
        
        // No token should be rejected
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }
    
    // Helper class for login requests
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
}