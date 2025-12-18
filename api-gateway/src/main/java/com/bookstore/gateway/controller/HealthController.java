package com.bookstore.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "API Gateway");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed")
    public Mono<ResponseEntity<Map<String, Object>>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "API Gateway");
        
        // Check service registry
        List<String> services = discoveryClient.getServices();
        health.put("discoveredServices", services);
        
        // Check service instances
        Map<String, Integer> serviceInstances = services.stream()
            .collect(Collectors.toMap(
                service -> service,
                service -> discoveryClient.getInstances(service).size()
            ));
        health.put("serviceInstances", serviceInstances);
        
        // Check Redis connectivity
        return redisTemplate.opsForValue()
            .get("health-check")
            .map(value -> {
                health.put("redis", "UP");
                return ResponseEntity.ok(health);
            })
            .onErrorReturn(ResponseEntity.ok(health))
            .switchIfEmpty(Mono.fromCallable(() -> {
                health.put("redis", "UP");
                return ResponseEntity.ok(health);
            }))
            .onErrorReturn(ResponseEntity.ok(health));
    }

    @GetMapping("/routes")
    public Mono<ResponseEntity<Map<String, Object>>> routes() {
        return routeLocator.getRoutes()
            .collectList()
            .map(routes -> {
                Map<String, Object> response = new HashMap<>();
                response.put("totalRoutes", routes.size());
                response.put("routes", routes.stream()
                    .map(route -> {
                        Map<String, Object> routeInfo = new HashMap<>();
                        routeInfo.put("id", route.getId());
                        routeInfo.put("uri", route.getUri().toString());
                        routeInfo.put("predicates", route.getPredicate().toString());
                        return routeInfo;
                    })
                    .collect(Collectors.toList()));
                return ResponseEntity.ok(response);
            });
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        
        // Basic JVM metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("maxMemory", runtime.maxMemory());
        jvm.put("totalMemory", runtime.totalMemory());
        jvm.put("freeMemory", runtime.freeMemory());
        jvm.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvm.put("availableProcessors", runtime.availableProcessors());
        
        metrics.put("jvm", jvm);
        metrics.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(metrics);
    }
}