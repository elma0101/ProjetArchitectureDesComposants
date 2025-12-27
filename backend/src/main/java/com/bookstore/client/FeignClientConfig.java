package com.bookstore.client;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Feign clients with timeouts, retries, and error handling
 */
@Configuration
@Profile("!h2")
public class FeignClientConfig {
    
    /**
     * Configure request timeouts
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,  // connect timeout (5 seconds)
            10000, // read timeout (10 seconds)
            true   // follow redirects
        );
    }
    
    /**
     * Configure retry policy
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            100,   // initial interval (100ms)
            1000,  // max interval (1 second)
            3      // max attempts
        );
    }
    
    /**
     * Configure logging level for Feign clients
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
    
    /**
     * Custom error decoder for handling HTTP errors
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}