package com.bookstore.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
        ReactiveStringRedisTemplate template = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        
        return template;
    }
}