package com.bookstore.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "mySecretKeyForBookstoreApplicationThatShouldBeAtLeast256BitsLong";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400L);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String token = createValidToken("testuser", List.of("USER"));

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String username = "testuser";
        String token = createValidToken(username, List.of("USER"));

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldExtractRolesFromToken() {
        // Given
        List<String> roles = List.of("USER", "ADMIN");
        String token = createValidToken("testuser", roles);

        // When
        String extractedRoles = jwtUtil.getRolesFromToken(token);

        // Then
        assertEquals("USER,ADMIN", extractedRoles);
    }

    @Test
    void shouldDetectExpiredToken() {
        // Given
        String expiredToken = createExpiredToken("testuser", List.of("USER"));

        // When & Then - Should throw ExpiredJwtException when trying to parse expired token
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtUtil.isTokenExpired(expiredToken);
        });
    }

    private String createValidToken(String username, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
            .signWith(key)
            .compact();
    }

    private String createExpiredToken(String username, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(new Date(System.currentTimeMillis() - 86400000)) // 24 hours ago
            .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
            .signWith(key)
            .compact();
    }
}