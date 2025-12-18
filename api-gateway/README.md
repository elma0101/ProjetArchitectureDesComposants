# API Gateway

The API Gateway serves as the single entry point for all client requests to the bookstore microservices architecture. It provides routing, authentication, rate limiting, and circuit breaker functionality.

## Features

- **Request Routing**: Routes requests to appropriate microservices based on path patterns
- **JWT Authentication**: Validates JWT tokens and extracts user information
- **Rate Limiting**: Implements per-user/IP rate limiting using Redis
- **Circuit Breaker**: Provides fault tolerance with fallback responses
- **Health Monitoring**: Comprehensive health checks and metrics
- **Service Discovery**: Integrates with Eureka for dynamic service discovery

## Architecture

The API Gateway is built using Spring Cloud Gateway and provides the following capabilities:

### Routing Rules

- `/api/auth/**` → User Management Service (authentication endpoints)
- `/api/users/**` → User Management Service (user management)
- `/api/books/**`, `/api/authors/**` → Book Catalog Service
- `/api/loans/**` → Loan Management Service
- `/api/recommendations/**` → Recommendation Service
- `/api/audit/**` → Audit Service
- `/api/notifications/**` → Notification Service

### Security

- JWT token validation for protected endpoints
- User information extraction and forwarding to downstream services
- Excluded paths: `/api/auth/login`, `/api/auth/register`, `/api/health`, `/actuator`

### Rate Limiting

- Default: 100 requests per minute per user/IP
- Uses Redis for distributed rate limiting
- Graceful degradation if Redis is unavailable

### Circuit Breaker

- Configurable failure thresholds per service
- Automatic fallback responses when services are unavailable
- Health-based routing decisions

## Configuration

### Environment Variables

- `JWT_SECRET`: Secret key for JWT token validation
- `REDIS_HOST`: Redis server hostname (default: localhost)
- `REDIS_PORT`: Redis server port (default: 6379)
- `EUREKA_SERVER_URL`: Eureka server URL
- `CONFIG_SERVER_URL`: Config server URL

### Profiles

- `dev`: Development configuration with debug logging
- `prod`: Production configuration with optimized settings
- `test`: Test configuration for unit/integration tests

## Running the Application

### Prerequisites

- Java 17+
- Maven 3.6+
- Redis server
- Eureka server
- Config server (optional)

### Local Development

```bash
# Start Redis
docker run -d -p 6379:6379 redis:alpine

# Start the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t bookstore/api-gateway .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=your-secret-key \
  -e REDIS_HOST=redis \
  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
  bookstore/api-gateway
```

## API Endpoints

### Health Checks

- `GET /api/health` - Basic health status
- `GET /api/health/detailed` - Detailed health with service discovery info
- `GET /api/health/routes` - Current routing configuration
- `GET /api/health/metrics` - JVM and application metrics

### Actuator Endpoints

- `GET /actuator/health` - Spring Boot health endpoint
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/gateway/routes` - Gateway routes information

## Monitoring

The API Gateway exposes metrics in Prometheus format and integrates with:

- **Prometheus**: Metrics collection
- **Grafana**: Dashboards and visualization
- **Jaeger**: Distributed tracing
- **ELK Stack**: Centralized logging

### Key Metrics

- Request count and latency per route
- Circuit breaker state and events
- Rate limiting statistics
- JVM and system metrics

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Load Testing

The gateway includes rate limiting and circuit breaker patterns to handle high load scenarios. Test with tools like JMeter or Artillery.

## Troubleshooting

### Common Issues

1. **JWT Validation Failures**
   - Verify JWT_SECRET matches the one used by User Management Service
   - Check token expiration and format

2. **Service Discovery Issues**
   - Ensure Eureka server is running and accessible
   - Check service registration in Eureka dashboard

3. **Rate Limiting Not Working**
   - Verify Redis connectivity
   - Check Redis configuration and network access

4. **Circuit Breaker Always Open**
   - Review failure thresholds and timeout configurations
   - Check downstream service health

### Logs

Application logs are written to:
- Console (development)
- `logs/api-gateway.log` (production)

Enable debug logging for troubleshooting:
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.bookstore.gateway: DEBUG
```

## Development

### Adding New Routes

1. Update `GatewayConfig.java` with new route definitions
2. Add appropriate filters (authentication, rate limiting, circuit breaker)
3. Update tests and documentation

### Custom Filters

Implement `GatewayFilter` or extend `AbstractGatewayFilterFactory` for custom functionality.

### Configuration

Use Spring Cloud Config for externalized configuration management across environments.