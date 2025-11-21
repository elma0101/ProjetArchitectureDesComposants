package com.bookstore.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityConfigurationTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private RateLimitingFilter rateLimitingFilter;
    
    @Test
    void testPasswordEncoderConfiguration() {
        assertNotNull(passwordEncoder);
        
        String rawPassword = "testpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }
    
    @Test
    void testAuthenticationManagerConfiguration() {
        assertNotNull(authenticationManager);
    }
    
    @Test
    void testJwtUtilConfiguration() {
        assertNotNull(jwtUtil);
    }
    
    @Test
    void testSecurityFiltersConfiguration() {
        assertNotNull(jwtAuthenticationFilter);
        assertNotNull(rateLimitingFilter);
    }
    
    @Test
    void testPasswordStrength() {
        String weakPassword = "123";
        String strongPassword = "StrongP@ssw0rd123!";
        
        String encodedWeak = passwordEncoder.encode(weakPassword);
        String encodedStrong = passwordEncoder.encode(strongPassword);
        
        // Both should be encoded, but in a real application,
        // you would have password strength validation
        assertNotNull(encodedWeak);
        assertNotNull(encodedStrong);
        
        // Verify that the same password doesn't produce the same hash (salt is used)
        String encodedWeak2 = passwordEncoder.encode(weakPassword);
        assertNotEquals(encodedWeak, encodedWeak2);
    }
    
    @Test
    void testPasswordEncodingConsistency() {
        String password = "testpassword";
        String encoded = passwordEncoder.encode(password);
        
        // The same password should always match the encoded version
        assertTrue(passwordEncoder.matches(password, encoded));
        
        // Different passwords should not match
        assertFalse(passwordEncoder.matches("differentpassword", encoded));
        assertFalse(passwordEncoder.matches("", encoded));
        assertFalse(passwordEncoder.matches(null, encoded));
    }
}