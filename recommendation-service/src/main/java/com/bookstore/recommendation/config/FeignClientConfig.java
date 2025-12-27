package com.bookstore.recommendation.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients to add service authentication headers
 */
@Configuration
public class FeignClientConfig {
    
    @Value("${security.service.api-key}")
    private String serviceApiKey;
    
    @Value("${spring.application.name}")
    private String serviceName;
    
    @Bean
    public RequestInterceptor serviceAuthenticationInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-Service-API-Key", serviceApiKey);
                template.header("X-Service-Name", serviceName);
            }
        };
    }
}
