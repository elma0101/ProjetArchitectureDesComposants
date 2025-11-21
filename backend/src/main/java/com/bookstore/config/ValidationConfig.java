package com.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Configuration for validation settings
 */
@Configuration
public class ValidationConfig {
    
    /**
     * Configure the validator factory bean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        // Additional configuration can be added here if needed
        return validatorFactoryBean;
    }
    
    /**
     * Enable method-level validation
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
    
    /**
     * Provide the validator bean for injection
     */
    @Bean
    public Validator validatorBean() {
        return validator();
    }
}