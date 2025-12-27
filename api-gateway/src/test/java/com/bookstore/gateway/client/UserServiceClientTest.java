package com.bookstore.gateway.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class UserServiceClientTest {

    private WireMockServer wireMockServer;
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);

        WebClient.Builder webClientBuilder = WebClient.builder();
        userServiceClient = new UserServiceClient(webClientBuilder);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true, \"username\": \"testuser\", \"roles\": \"USER\", \"userId\": \"1\"}")));

        // When
        var result = userServiceClient.validateToken("valid-token");

        // Then
        StepVerifier.create(result)
            .expectNextMatches(response -> 
                response.isValid() && 
                "testuser".equals(response.getUsername()) &&
                "USER".equals(response.getRoles()))
            .verifyComplete();
    }

    @Test
    void shouldHandleInvalidToken() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": false}")));

        // When
        var result = userServiceClient.validateToken("invalid-token");

        // Then
        StepVerifier.create(result)
            .expectNextMatches(response -> !response.isValid())
            .verifyComplete();
    }

    @Test
    void shouldHandleServiceUnavailable() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/validate"))
            .willReturn(aResponse()
                .withStatus(503)));

        // When
        var result = userServiceClient.validateToken("test-token");

        // Then
        StepVerifier.create(result)
            .verifyComplete(); // Should complete empty on error
    }

    @Test
    void shouldHandleTimeout() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/auth/validate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(5000))); // Delay longer than timeout

        // When
        var result = userServiceClient.validateToken("test-token");

        // Then
        StepVerifier.create(result)
            .verifyComplete(); // Should complete empty on timeout
    }

    @Test
    void shouldGetUserByUsername() {
        // Given
        wireMockServer.stubFor(get(urlPathEqualTo("/api/users/username/testuser"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"username\": \"testuser\", \"email\": \"test@test.com\"}")));

        // When
        var result = userServiceClient.getUserByUsername("testuser", "valid-token");

        // Then
        StepVerifier.create(result)
            .expectNextMatches(user -> 
                user.get("username").equals("testuser") &&
                user.get("email").equals("test@test.com"))
            .verifyComplete();
    }
}
