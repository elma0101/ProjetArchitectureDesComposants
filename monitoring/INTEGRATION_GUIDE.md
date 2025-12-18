# Monitoring Integration Guide for Microservices

This guide explains how to integrate your microservices with the monitoring stack.

## Prerequisites

- Spring Boot 3.x
- Maven or Gradle
- Monitoring stack deployed and running

## 1. Add Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<!-- Actuator for metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Distributed Tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>

<!-- Logstash Logback Encoder -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

## 2. Configure Application Properties

Add to your `application.yml`:

```yaml
spring:
  application:
    name: your-service-name

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:development}
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://jaeger:9411/api/v2/spans

logging:
  level:
    root: INFO
    com.bookstore: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## 3. Configure Logback for Logstash

Create `src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <springProperty scope="context" name="springAppName" source="spring.application.name"/>
    <springProperty scope="context" name="logstashHost" source="logstash.host" defaultValue="localhost"/>
    <springProperty scope="context" name="logstashPort" source="logstash.port" defaultValue="5000"/>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Logstash Appender -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>${logstashHost}:${logstashPort}</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${springAppName}"}</customFields>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
    
    <!-- Async Logstash Appender -->
    <appender name="ASYNC_LOGSTASH" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="LOGSTASH"/>
        <queueSize>512</queueSize>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_LOGSTASH"/>
    </root>
</configuration>
```

## 4. Add Custom Metrics

### Counter Example

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    
    private final Counter bookCreatedCounter;
    
    public BookService(MeterRegistry meterRegistry) {
        this.bookCreatedCounter = Counter.builder("books.created")
            .description("Number of books created")
            .tag("service", "book-catalog")
            .register(meterRegistry);
    }
    
    public void createBook(Book book) {
        // Business logic
        bookCreatedCounter.increment();
    }
}
```

### Timer Example

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class LoanService {
    
    private final Timer loanProcessingTimer;
    
    public LoanService(MeterRegistry meterRegistry) {
        this.loanProcessingTimer = Timer.builder("loan.processing.time")
            .description("Time taken to process loan")
            .tag("service", "loan-management")
            .register(meterRegistry);
    }
    
    public void processLoan(Loan loan) {
        loanProcessingTimer.record(() -> {
            // Business logic
        });
    }
}
```

### Gauge Example

```java
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    
    private final AtomicInteger availableBooks = new AtomicInteger(0);
    
    public InventoryService(MeterRegistry meterRegistry) {
        Gauge.builder("books.available", availableBooks, AtomicInteger::get)
            .description("Number of available books")
            .tag("service", "book-catalog")
            .register(meterRegistry);
    }
}
```

## 5. Add Distributed Tracing

### Automatic Tracing

Spring Boot automatically traces:
- HTTP requests
- Database queries
- Message queue operations

### Custom Spans

```java
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {
    
    private final Tracer tracer;
    
    public RecommendationService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public List<Book> getRecommendations(Long userId) {
        Span span = tracer.nextSpan().name("get-recommendations");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            span.tag("user.id", userId.toString());
            // Business logic
            return recommendations;
        } finally {
            span.end();
        }
    }
}
```

## 6. Structured Logging

### Use SLF4J with MDC

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(User user) {
        MDC.put("userId", user.getId().toString());
        MDC.put("operation", "create-user");
        
        try {
            log.info("Creating user: {}", user.getUsername());
            // Business logic
            log.info("User created successfully");
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

## 7. Health Checks

### Custom Health Indicator

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            boolean isHealthy = checkDatabaseConnection();
            
            if (isHealthy) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Disconnected")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private boolean checkDatabaseConnection() {
        // Implementation
        return true;
    }
}
```

## 8. Kubernetes Annotations

Add these annotations to your Kubernetes deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: your-service
spec:
  template:
    metadata:
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
        - name: your-service
          image: your-service:latest
          ports:
            - containerPort: 8080
          env:
            - name: LOGSTASH_HOST
              value: "logstash.monitoring.svc.cluster.local"
            - name: JAEGER_ENDPOINT
              value: "http://jaeger.monitoring.svc.cluster.local:9411/api/v2/spans"
```

## 9. Testing Integration

### Test Metrics Endpoint

```bash
curl http://localhost:8080/actuator/prometheus
```

### Test Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### Test Tracing

1. Make a request to your service
2. Open Jaeger UI: http://localhost:16686
3. Search for traces from your service

### Test Logging

1. Make a request to your service
2. Open Kibana: http://localhost:5601
3. Search for logs with your service name

## 10. Best Practices

### Metrics
- Use meaningful metric names
- Add appropriate tags for filtering
- Monitor both technical and business metrics
- Set up alerts for critical metrics

### Logging
- Use structured logging (JSON)
- Include correlation IDs
- Log at appropriate levels
- Avoid logging sensitive data

### Tracing
- Propagate trace context
- Add custom spans for important operations
- Include relevant tags
- Monitor sampling rate

### Health Checks
- Implement liveness and readiness probes
- Check external dependencies
- Return appropriate status codes
- Include diagnostic information

## Troubleshooting

### Metrics Not Appearing in Prometheus
1. Check actuator endpoint is accessible
2. Verify Prometheus configuration
3. Check network connectivity
4. Review service logs

### Logs Not in Kibana
1. Verify Logstash connectivity
2. Check log format
3. Review Logstash pipeline
4. Check Elasticsearch indices

### Traces Not in Jaeger
1. Verify Jaeger endpoint configuration
2. Check sampling rate
3. Review network connectivity
4. Check service logs for errors