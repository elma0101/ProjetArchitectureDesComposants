package com.bookstore.config;

import com.bookstore.versioning.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Configuration for API versioning and feature toggles
 */
@Configuration
@Profile("!h2")
public class ApiVersioningConfig implements WebMvcConfigurer, WebMvcRegistrations {

    @Autowired
    private FeatureToggleService featureToggleService;

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DeprecationWarningInterceptor())
                .addPathPatterns("/api/**");
        
        registry.addInterceptor(new FeatureToggleInterceptor(featureToggleService))
                .addPathPatterns("/api/**");
    }

    @Bean
    public FeatureToggleService featureToggleService() {
        return new FeatureToggleService();
    }
}