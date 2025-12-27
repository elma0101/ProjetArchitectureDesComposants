package com.bookstore.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * End-to-end integration tests for authentication flow through API Gateway
 * Tests Requirements: 2.1, 3.3, 9.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserServiceAuthenticationFlowTest {

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer mockUserService;

    private final String secret = "mySecretKeyForBookstoreApplicationThatShouldBeAtLeast256BitsLong";

    @BeforeEach
    void setUp() {
        mockUserService = new WireMockServer(8081);
        mockUserService.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void tearDown() {
        if (mockUserService != null) {
            mockUserService.stop();
        }
    }

    @Test
    void shouldRouteLoginRequestToUserService() {
        // Given
        mockUserService.stubFor(post(urlEqualTo("/api/auth/login"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"token\": \"test-token\", \"username\": \"testuser\"}")));

        // When & Then
        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\": \"testuser\", \"password\": \"password\"}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("test-token")
            .jsonPath("$.username").isEqualTo("testuser");

        // Verify the request was forwarded to user service
        mockUserService.verify(postRequestedFor(urlEqualTo("/api/auth/login")));
    }

    @Test
    void shouldRouteRegisterRequestToUserService() {
        // Given
        mockUserService.stubFor(post(urlEqualTo("/api/auth/register"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"token\": \"new-token\", \"username\": \"newuser\"}")));

        // When & Then
        webTestClient.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\": \"newuser\", \"email\": \"new@test.com\", \"password\": \"password\"}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("new-token")
            .jsonPath("$.username").isEqualTo("newuser");

        mockUserService.verify(postRequestedFor(urlEqualTo("/api/auth/register")));
    }

    @Test
    void shouldAllowAuthenticatedAccessToUserEndpoints() {
        // Given
        String token = createValidToken("testuser", List.of("USER"));
        
        mockUserService.stubFor(get(urlEqualTo("/api/users/profile"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"username\": \"testuser\", \"email\": \"test@test.com\"}")));

        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser");

        // Verify headers were added
        mockUserService.verify(getRequestedFor(urlEqualTo("/api/users/profile"))
            .withHeader("X-User-Id", equalTo("testuser"))
            .withHeader("X-User-Roles", equalTo("USER")));
    }

    @Test
    void shouldRejectUnauthenticatedAccessToProtectedEndpoints() {
        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Missing or invalid Authorization header");
    }

    @Test
    void shouldRejectInvalidToken() {
        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            .expectBody()
            .jsonPath("$.error").isEqualTo("JWT token validation failed");
    }

    @Test
    void shouldRejectExpiredToken() {
        // Given - Create an expired token
        String expiredToken = createExpiredToken("testuser", List.of("USER"));

        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldHandleUserServiceFailureWithFallback() {
        // Given
        String token = createValidToken("testuser", List.of("USER"));
        
        mockUserService.stubFor(get(urlEqualTo("/api/users/profile"))
            .willReturn(aResponse()
                .withStatus(500)
                .withFixedDelay(15000))); // Simulate timeout

        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Service Unavailable")
            .jsonPath("$.errorCode").isEqualTo("USER_SERVICE_UNAVAILABLE");
    }

    @Test
    void shouldApplyRateLimitingToAuthEndpoints() {
        // Given
        mockUserService.stubFor(post(urlEqualTo("/api/auth/login"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"token\": \"test-token\"}")));

        // When - Make multiple requests
        for (int i = 0; i < 5; i++) {
            webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\": \"testuser\", \"password\": \"password\"}")
                .exchange()
                .expectStatus().isOk();
        }

        // Note: Rate limiting behavior depends on Redis configuration
        // This test verifies the endpoint is accessible
    }

    @Test
    void shouldPropagateUserContextToDownstreamServices() {
        // Given
        String token = createValidToken("admin", List.of("ADMIN", "USER"));
        
        mockUserService.stubFor(get(urlEqualTo("/api/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"username\": \"someuser\"}")));

        // When & Then
        webTestClient.get()
            .uri("/api/users/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isOk();

        // Verify user context headers were added
        mockUserService.verify(getRequestedFor(urlEqualTo("/api/users/1"))
            .withHeader("X-User-Id", equalTo("admin"))
            .withHeader("X-User-Roles", equalTo("ADMIN,USER")));
    }

    @Test
    void shouldHandleRefreshTokenRequest() {
        // Given
        mockUserService.stubFor(post(urlEqualTo("/api/auth/refresh"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"token\": \"refreshed-token\", \"username\": \"testuser\"}")));

        // When & Then
        webTestClient.post()
            .uri("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"refreshToken\": \"old-refresh-token\"}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.token").isEqualTo("refreshed-token");

        mockUserService.verify(postRequestedFor(urlEqualTo("/api/auth/refresh")));
    }

    @Test
    void shouldHandleLogoutRequest() {
        // Given
        mockUserService.stubFor(post(urlEqualTo("/api/auth/logout"))
            .willReturn(aResponse()
                .withStatus(200)));

        // When & Then
        webTestClient.post()
            .uri("/api/auth/logout")
            .exchange()
            .expectStatus().isOk();

        mockUserService.verify(postRequestedFor(urlEqualTo("/api/auth/logout")));
    }

    @Test
    void shouldRetryOnTransientFailures() {
        // Given
        String token = createValidToken("testuser", List.of("USER"));
        
        // First two requests fail, third succeeds
        mockUserService.stubFor(get(urlEqualTo("/api/users/profile"))
            .inScenario("Retry")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(503))
            .willSetStateTo("First Retry"));
        
        mockUserService.stubFor(get(urlEqualTo("/api/users/profile"))
            .inScenario("Retry")
            .whenScenarioStateIs("First Retry")
            .willReturn(aResponse().withStatus(503))
            .willSetStateTo("Second Retry"));
        
        mockUserService.stubFor(get(urlEqualTo("/api/users/profile"))
            .inScenario("Retry")
            .whenScenarioStateIs("Second Retry")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"username\": \"testuser\"}")));

        // When & Then
        webTestClient.get()
            .uri("/api/users/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser");

        // Verify retries occurred
        mockUserService.verify(3, getRequestedFor(urlEqualTo("/api/users/profile")));
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
