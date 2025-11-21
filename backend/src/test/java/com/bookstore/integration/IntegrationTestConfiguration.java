package com.bookstore.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

/**
 * Test configuration for integration tests
 */
@TestConfiguration
@Profile("integration-test")
@TestPropertySource(locations = "classpath:application-integration-test.yml")
public class IntegrationTestConfiguration {

    /**
     * Configure test-specific beans if needed
     */
    
    // Example: Override certain beans for testing
    // @Bean
    // @Primary
    // public SomeService mockSomeService() {
    //     return Mockito.mock(SomeService.class);
    // }
}