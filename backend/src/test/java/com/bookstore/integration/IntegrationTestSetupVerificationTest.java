package com.bookstore.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify integration test setup works correctly
 */
@SpringBootTest
@ActiveProfiles("integration-test")
class IntegrationTestSetupVerificationTest extends BaseIntegrationTest {

    @Test
    void shouldStartApplicationContext() {
        // This test verifies that the Spring application context starts successfully
        // with TestContainers and all integration test configurations
        assertThat(baseUrl).isNotNull();
        assertThat(port).isGreaterThan(0);
        assertThat(mockMvc).isNotNull();
        assertThat(restTemplate).isNotNull();
        assertThat(objectMapper).isNotNull();
        assertThat(dataSource).isNotNull();
    }

    @Test
    void shouldHaveTestContainerRunning() {
        // Verify that PostgreSQL TestContainer is running
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getJdbcUrl()).isNotNull();
        assertThat(postgres.getUsername()).isEqualTo("test");
        assertThat(postgres.getPassword()).isEqualTo("test");
    }

    @Test
    void shouldHaveCorrectProfile() {
        // Verify that the integration-test profile is active
        assertThat(System.getProperty("spring.profiles.active")).contains("integration-test");
    }
}