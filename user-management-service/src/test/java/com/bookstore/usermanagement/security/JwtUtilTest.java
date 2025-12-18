package com.bookstore.usermanagement.security;

import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private User testUser;
    private Authentication authentication;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "mySecretKeyForJWTTokenGenerationAndValidation1234567890");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMs", 604800000);
        
        testUser = new User("testuser", "password", "test@example.com", Set.of(Role.USER));
        authentication = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
    }
    
    @Test
    void generateJwtToken_Success() {
        // When
        String token = jwtUtil.generateJwtToken(authentication);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void generateTokenFromUsername_Success() {
        // When
        String token = jwtUtil.generateTokenFromUsername("testuser");
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void generateRefreshToken_Success() {
        // When
        String refreshToken = jwtUtil.generateRefreshToken("testuser");
        
        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }
    
    @Test
    void getUsernameFromJwtToken_Success() {
        // Given
        String token = jwtUtil.generateTokenFromUsername("testuser");
        
        // When
        String username = jwtUtil.getUsernameFromJwtToken(token);
        
        // Then
        assertEquals("testuser", username);
    }
    
    @Test
    void validateJwtToken_ValidToken() {
        // Given
        String token = jwtUtil.generateTokenFromUsername("testuser");
        
        // When
        boolean isValid = jwtUtil.validateJwtToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void validateJwtToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtUtil.validateJwtToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void isTokenExpired_ValidToken() {
        // Given
        String token = jwtUtil.generateTokenFromUsername("testuser");
        
        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // Then
        assertFalse(isExpired);
    }
}