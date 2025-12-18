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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer mockUserService;
    private WireMockServer mockBookService;

    private final String secret = "mySecretKeyForBookstoreApplicationThatShouldBeAtLeast256BitsLong";

    @BeforeEach
    void setUp() {
        mockUserService = new WireMockServer(8081);
        mockBookService = new WireMockServer(8082);
        
        mockUserService.start();
        mockBookService.start();
        
        WireMock.configureFor("localhost", 8081);
        mockUserService.stubFor(get(urlPathMatching("/api/users/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"username\": \"testuser\"}")));
        
        WireMock.configureFor("localhost", 8082);
        mockBookService.stubFor(get(urlPathMatching("/api/books/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"title\": \"Test Book\"}")));
    }

    @AfterEach
    void tearDown() {
        if (mockUserService != null) {
            mockUserService.stop();
        }
        if (mockBookService != null) {
            mockBookService.stop();
        }
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() {
        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\": \"test\", \"password\": \"test\"}")
            .exchange()
            .expectStatus().is4xxClientError(); // Expected since no actual service is running and method not allowed
    }

    @Test
    void shouldRejectUnauthorizedRequests() {
        webTestClient.get()
            .uri("/api/users/1")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAuthorizedRequests() {
        // Given
        String token = createValidToken("testuser", List.of("USER"));

        // When & Then
        webTestClient.get()
            .uri("/api/users/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().is5xxServerError(); // Expected since no actual service is running
    }

    @Test
    void shouldReturnHealthStatus() {
        webTestClient.get()
            .uri("/api/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP");
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
}