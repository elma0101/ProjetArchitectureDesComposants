package com.bookstore.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(HealthController.class)
@ActiveProfiles("test")
@Import(com.bookstore.gateway.config.TestConfig.class)
class HealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DiscoveryClient discoveryClient;

    @MockBean
    private RouteLocator routeLocator;

    @MockBean
    private ReactiveStringRedisTemplate redisTemplate;

    @Test
    void shouldReturnHealthStatus() {
        webTestClient.get()
            .uri("/api/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.service").isEqualTo("API Gateway")
            .jsonPath("$.version").isEqualTo("1.0.0");
    }

    @Test
    void shouldReturnDetailedHealth() {
        // Given
        when(discoveryClient.getServices()).thenReturn(List.of("user-service", "book-service"));
        when(discoveryClient.getInstances("user-service")).thenReturn(List.of());
        when(discoveryClient.getInstances("book-service")).thenReturn(List.of());

        // When & Then
        webTestClient.get()
            .uri("/api/health/detailed")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.discoveredServices").isArray()
            .jsonPath("$.serviceInstances").exists();
    }

    @Test
    void shouldReturnMetrics() {
        webTestClient.get()
            .uri("/api/health/metrics")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.jvm").exists()
            .jsonPath("$.jvm.maxMemory").exists()
            .jsonPath("$.jvm.totalMemory").exists()
            .jsonPath("$.jvm.freeMemory").exists()
            .jsonPath("$.uptime").exists();
    }
}