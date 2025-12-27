# Audit Service

Centralized audit logging service for the bookstore microservices architecture.

## Overview

The Audit Service provides centralized audit logging capabilities using Elasticsearch for storage and RabbitMQ for asynchronous log collection from other microservices. It enables comprehensive tracking of user actions, system events, and security-related activities across the entire platform.

## Features

- **Centralized Audit Logging**: Collect audit logs from all microservices
- **Elasticsearch Storage**: Fast, scalable storage and search capabilities
- **Message Queue Integration**: Asynchronous log collection via RabbitMQ
- **Rich Search API**: Search logs by user, action, resource, date range, and more
- **Correlation ID Tracking**: Track related events across services
- **Data Migration**: Tools for migrating existing audit logs from monolith
- **Service Discovery**: Automatic registration with Eureka
- **Health Monitoring**: Actuator endpoints for health checks and metrics

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data Elasticsearch**
- **Spring AMQP (RabbitMQ)**
- **Spring Cloud Netflix Eureka**
- **Lombok**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Elasticsearch 8.x
- RabbitMQ 3.12+
- Eureka Server (for service discovery)

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

server:
  port: 8085

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Environment Variables

- `ELASTICSEARCH_URIS`: Elasticsearch connection URI
- `RABBITMQ_HOST`: RabbitMQ host
- `RABBITMQ_PORT`: RabbitMQ port
- `RABBITMQ_USERNAME`: RabbitMQ username
- `RABBITMQ_PASSWORD`: RabbitMQ password
- `EUREKA_SERVER_URL`: Eureka server URL

## Building and Running

### Local Development

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
# Build Docker image
./build.sh

# Run with Docker Compose
docker-compose up -d
```

### Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml
```

## API Endpoints

### Create Audit Log

```http
POST /api/audit/logs
Content-Type: application/json

{
  "userId": 1,
  "username": "john.doe",
  "action": "CREATE",
  "resourceType": "BOOK",
  "resourceId": "123",
  "serviceName": "book-catalog-service",
  "description": "Created a new book",
  "ipAddress": "192.168.1.1",
  "correlationId": "abc-123",
  "timestamp": "2024-11-29T10:30:00",
  "severity": "INFO",
  "success": true
}
```

### Search Audit Logs

```http
POST /api/audit/logs/search
Content-Type: application/json

{
  "userId": 1,
  "startDate": "2024-11-01T00:00:00",
  "endDate": "2024-11-30T23:59:59",
  "page": 0,
  "size": 20,
  "sortBy": "timestamp",
  "sortDirection": "DESC"
}
```

### Get Logs by User ID

```http
GET /api/audit/logs/user/{userId}?page=0&size=20
```

### Get Logs by Correlation ID

```http
GET /api/audit/logs/correlation/{correlationId}
```

### Get Logs by Date Range

```http
GET /api/audit/logs/date-range?start=2024-11-01T00:00:00&end=2024-11-30T23:59:59&page=0&size=20
```

### Health Check

```http
GET /api/health
```

## Message Queue Integration

### Publishing Audit Logs

Other microservices can publish audit logs to RabbitMQ:

```java
@Autowired
private RabbitTemplate rabbitTemplate;

public void logAction(AuditLogRequest auditLog) {
    rabbitTemplate.convertAndSend(
        "audit.exchange",
        "audit.log",
        auditLog
    );
}
```

### Queue Configuration

- **Exchange**: `audit.exchange` (Topic)
- **Queue**: `audit.queue`
- **Routing Key**: `audit.#`

## Data Migration

To migrate existing audit logs from the monolith:

```http
POST /api/audit/migration/migrate
Content-Type: application/json

[
  {
    "user_id": 1,
    "username": "john.doe",
    "action": "CREATE",
    "resource_type": "BOOK",
    "resource_id": "123",
    "timestamp": "2024-11-29T10:30:00"
  }
]
```

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Prometheus Metrics

The service exposes Prometheus-compatible metrics for monitoring:

- Request counts and durations
- Elasticsearch operation metrics
- RabbitMQ message processing metrics
- JVM and system metrics

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuditLogServiceTest

# Run with coverage
mvn test jacoco:report
```

## Security Considerations

- Audit logs are immutable once created
- Sensitive data should be masked before logging
- Access to audit logs should be restricted to authorized users
- Implement retention policies for log data
- Use secure connections (TLS) for Elasticsearch and RabbitMQ in production

## Troubleshooting

### Elasticsearch Connection Issues

```bash
# Check Elasticsearch status
curl http://localhost:9200/_cluster/health

# View Elasticsearch logs
docker logs elasticsearch
```

### RabbitMQ Connection Issues

```bash
# Check RabbitMQ status
docker exec rabbitmq rabbitmqctl status

# View RabbitMQ management UI
http://localhost:15672
```

### Service Not Registering with Eureka

- Verify Eureka server is running
- Check network connectivity
- Review application logs for registration errors

## Contributing

1. Follow the existing code style
2. Write unit tests for new features
3. Update documentation as needed
4. Submit pull requests for review

## License

Copyright © 2024 Bookstore. All rights reserved.
