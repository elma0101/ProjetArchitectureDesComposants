package com.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for enabling asynchronous processing and scheduled tasks.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Spring Boot's default async configuration is sufficient for our needs
    // The configuration is handled through application.yml properties
}