package com.bookstore.security;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtUtilTest {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "test@test.com", Set.of(Role.USER));
    }
    
    @Test
    void testTokenGeneration() {
        String token = jwtUtil.generateToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }
    
    @Test
    void testTokenGenerationWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("department", "IT");
        
        String token = jwtUtil.generateToken(testUser, extraClaims);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testUsernameExtraction() {
        String token = jwtUtil.generateToken(testUser);
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertEquals(testUser.getUsername(), extractedUsername);
    }
    
    @Test
    void testExpirationExtraction() {
        String token = jwtUtil.generateToken(testUser);
        Date expiration = jwtUtil.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }
    
    @Test
    void testTokenValidation() {
        String token = jwtUtil.generateToken(testUser);
        
        assertTrue(jwtUtil.validateToken(token, testUser));
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void testInvalidTokenValidation() {
        String invalidToken = "invalid.jwt.token";
        
        assertFalse(jwtUtil.validateToken(invalidToken));
        assertFalse(jwtUtil.validateToken(invalidToken, testUser));
    }
    
    @Test
    void testTokenValidationWithWrongUser() {
        User anotherUser = new User("anotheruser", "password", "another@test.com", Set.of(Role.USER));
        String token = jwtUtil.generateToken(testUser);
        
        assertFalse(jwtUtil.validateToken(token, anotherUser));
    }
    
    @Test
    void testTokenStructure() {
        String token = jwtUtil.generateToken(testUser);
        String[] parts = token.split("\\.");
        
        assertEquals(3, parts.length); // Header, Payload, Signature
        
        // Each part should be base64 encoded (no spaces or special chars except allowed ones)
        for (String part : parts) {
            assertTrue(part.matches("[A-Za-z0-9_-]+"));
        }
    }
    
    @Test
    void testMultipleTokensForSameUser() {
        String token1 = jwtUtil.generateToken(testUser);
        String token2 = jwtUtil.generateToken(testUser);
        
        // Tokens should be different (due to timestamp)
        assertNotEquals(token1, token2);
        
        // But both should be valid
        assertTrue(jwtUtil.validateToken(token1, testUser));
        assertTrue(jwtUtil.validateToken(token2, testUser));
    }
}