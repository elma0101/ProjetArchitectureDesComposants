package com.bookstore.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(ExternalConfigurationProperties.class)
public class ConfigurationManagementConfig {

    @Bean
    @Profile("prod")
    public ConfigurationValidator configurationValidator(Environment environment) {
        return new ConfigurationValidator(environment);
    }

    public static class ConfigurationValidator {
        private final Environment environment;

        public ConfigurationValidator(Environment environment) {
            this.environment = environment;
            validateRequiredProperties();
        }

        private void validateRequiredProperties() {
            String[] requiredProperties = {
                "spring.datasource.url",
                "spring.datasource.username",
                "spring.datasource.password",
                "spring.security.jwt.secret"
            };

            for (String property : requiredProperties) {
                String value = environment.getProperty(property);
                if (value == null || value.trim().isEmpty()) {
                    throw new IllegalStateException(
                        "Required configuration property '" + property + "' is not set"
                    );
                }
            }

            // Validate JWT secret length
            String jwtSecret = environment.getProperty("spring.security.jwt.secret");
            if (jwtSecret != null && jwtSecret.length() < 32) {
                throw new IllegalStateException(
                    "JWT secret must be at least 32 characters long for security"
                );
            }
        }
    }
}