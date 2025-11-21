# Bookstore Application Monitoring and Observability

This document describes the monitoring and observability features implemented in the Bookstore application.

## Overview

The monitoring system provides comprehensive observability through:
- **Metrics Collection**: Custom business metrics and system metrics
- **Health Checks**: Application and dependency health monitoring
- **Distributed Tracing**: Request tracing across services
- **Structured Logging**: JSON-formatted logs with correlation IDs
- **Alerting**: Automated alerts for critical system events
- **Dashboards**: Visual monitoring through Grafana

## Components

### 1. Spring Boot Actuator
- **Endpoints**: Health, metrics, info, prometheus, and more
- **Configuration**: Located in `application.yml`
- **Access**: Available at `/actuator/*` endpoints

### 2. Micrometer Metrics
- **Custom Metrics**: Business-specific counters, timers, and gauges
- **System Metrics**: JVM, HTTP, database connection pool metrics
- **Export**: Prometheus format for scraping

### 3. Distributed Tracing
- **Technology**: Micrometer Tracing with Zipkin
- **Correlation**: Trace and span IDs in logs
- **Visualization**: Zipkin UI for trace analysis

### 4. Structured Logging
- **Format**: JSON with Logstash encoder
- **Correlation**: Trace IDs, user IDs, operation context
- **Levels**: Configurable per package/class

### 5. Monitoring Infrastructure
- **Prometheus**: Metrics collection and storage
- **Grafana**: Visualization and dashboards
- **AlertManager**: Alert routing and notification
- **Zipkin**: Distributed tracing collection

## Custom Metrics

### Business Metrics
- `bookstore.loans.total` - Total book loans counter
- `bookstore.returns.total` - Total book returns counter
- `bookstore.users.registrations` - User registration counter
- `bookstore.users.active` - Currently active users gauge
- `bookstore.loans.current` - Current active loans gauge

### Performance Metrics
- `bookstore.search.duration` - Book search operation timing
- `bookstore.database.operation.duration` - Database operation timing
- `bookstore.api.errors` - API error counter
- `bookstore.external.calls` - External service call timing

## Health Checks

### Built-in Health Indicators
- **Database**: PostgreSQL connection health
- **Redis**: Cache connection health
- **Disk Space**: Available disk space
- **Custom**: Application-specific health checks

### Custom Health Indicators
- **Application Health**: Business logic health validation
- **External Dependencies**: Third-party service availability

## Alerting Rules

### Critical Alerts
- High error rate (>10% in 5 minutes)
- Database connection down
- Circuit breaker open
- Low disk space (<10%)

### Warning Alerts
- High response time (>2 seconds 95th percentile)
- High memory usage (>80% heap)
- High CPU usage (>80%)
- Slow database operations (>1 second 95th percentile)

## Setup Instructions

### 1. Start Monitoring Infrastructure

```bash
# Start monitoring stack
docker-compose -f docker-compose.monitoring.yml up -d

# Verify services are running
docker-compose -f docker-compose.monitoring.yml ps
```

### 2. Access Monitoring Tools

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Zipkin**: http://localhost:9411
- **AlertManager**: http://localhost:9093

### 3. Application Endpoints

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Custom Monitoring**: http://localhost:8080/api/admin/monitoring/*

## Configuration

### Application Configuration (`application.yml`)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,caches,env,configprops,loggers
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Logging Configuration (`logback-spring.xml`)

- JSON structured logging
- Trace correlation
- Async appenders for performance
- Profile-specific configurations

### Prometheus Configuration (`monitoring/prometheus.yml`)

- Scrape configurations for application and infrastructure
- Alert rule definitions
- Service discovery settings

## Usage Examples

### Recording Custom Metrics

```java
@Autowired
private MonitoringService monitoringService;

// Record business events
monitoringService.recordBookLoan("user123", "book456");
monitoringService.recordUserLogin("user123");

// Time operations
Timer.Sample sample = monitoringService.startSearchTimer();
// ... perform search ...
monitoringService.recordSearchTime(sample, "title", resultCount);
```

### Accessing Monitoring Data

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Get metrics summary
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/admin/monitoring/metrics/summary

# Trigger test alert
curl -X POST -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/admin/monitoring/test-alert?alertType=TEST"
```

### Querying Prometheus

```promql
# Error rate over time
rate(bookstore_api_errors_total[5m])

# Response time percentiles
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Active users
bookstore_users_active

# Database operation timing
rate(bookstore_database_operation_duration_seconds_sum[5m]) / 
rate(bookstore_database_operation_duration_seconds_count[5m])
```

## Grafana Dashboards

### Application Dashboard
- Request rate and response times
- Error rates and types
- Business metrics (loans, users, searches)
- JVM metrics (memory, threads, GC)

### Infrastructure Dashboard
- System metrics (CPU, memory, disk)
- Database performance
- Cache hit rates
- Network metrics

### Alerts Dashboard
- Active alerts
- Alert history
- Alert resolution times

## Log Analysis

### Structured Log Format

```json
{
  "timestamp": "2023-12-07T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.bookstore.service.BookService",
  "message": "Book loan recorded",
  "traceId": "abc123",
  "spanId": "def456",
  "userId": "user123",
  "bookId": "book456",
  "operation": "loan"
}
```

### Log Correlation

- **Trace ID**: Links all logs for a single request
- **Span ID**: Identifies specific operation within trace
- **User ID**: Associates logs with specific users
- **Operation**: Categorizes log entries by business function

## Troubleshooting

### Common Issues

1. **Metrics not appearing in Prometheus**
   - Check application `/actuator/prometheus` endpoint
   - Verify Prometheus scrape configuration
   - Check network connectivity

2. **Traces not appearing in Zipkin**
   - Verify Zipkin endpoint configuration
   - Check sampling probability setting
   - Ensure Zipkin is running and accessible

3. **Alerts not firing**
   - Check AlertManager configuration
   - Verify alert rule syntax
   - Check notification channels

### Debug Commands

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check AlertManager status
curl http://localhost:9093/api/v1/status

# Test application metrics endpoint
curl http://localhost:8080/actuator/prometheus | grep bookstore
```

## Security Considerations

- Monitoring endpoints require admin authentication
- Sensitive information excluded from metrics and logs
- Network security for monitoring infrastructure
- Access control for monitoring dashboards

## Performance Impact

- Metrics collection: Minimal overhead (<1% CPU)
- Distributed tracing: Configurable sampling rate
- Structured logging: Async appenders for performance
- Health checks: Cached results to reduce load

## Maintenance

### Regular Tasks
- Monitor disk usage for metrics storage
- Review and update alert thresholds
- Archive old logs and metrics
- Update monitoring infrastructure

### Capacity Planning
- Metrics retention: 200 hours default
- Log retention: 30 days default
- Trace retention: Based on Zipkin configuration
- Dashboard refresh rates: Optimized for performance