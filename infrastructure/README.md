# Infrastructure Services

This directory contains the infrastructure services required for the microservices architecture:

- **Eureka Server**: Service registry and discovery
- **Config Server**: Centralized configuration management
- **RabbitMQ**: Message broker for asynchronous communication

## Services Overview

### Eureka Server (Port 8761)
- Service registry for microservice discovery
- Health monitoring and load balancing
- Web dashboard for service monitoring
- Basic authentication enabled

### Config Server (Port 8888)
- Centralized configuration management
- Git-based configuration repository support
- Environment-specific configurations
- Configuration refresh without restart

### RabbitMQ (Ports 5672, 15672)
- Message broker for asynchronous inter-service communication
- Topic-based routing for flexible message distribution
- Dead letter queue handling for failed messages
- High availability clustering support
- Management UI for monitoring and administration

## Local Development

### Using Docker Compose

1. Build and start infrastructure services:
```bash
cd infrastructure
docker-compose -f docker-compose.infrastructure.yml up -d
```

2. Check service health:
```bash
# Eureka Server
curl http://eureka:eureka123@localhost:8761/actuator/health

# Config Server
curl http://config:config123@localhost:8888/actuator/health
```

3. Access web interfaces:
- Eureka Dashboard: http://localhost:8761 (eureka/eureka123)
- Config Server: http://localhost:8888 (config/config123)
- RabbitMQ Management: http://localhost:15672 (bookstore/bookstore123)

### Building Individual Services

#### Eureka Server
```bash
cd eureka-server
./mvnw clean package
docker build -t bookstore/eureka-server:latest .
```

#### Config Server
```bash
cd config-server
./mvnw clean package
docker build -t bookstore/config-server:latest .
```

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster running
- kubectl configured

### Deploy Infrastructure

1. Create namespace and secrets:
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/infrastructure-secrets.yaml
```

2. Deploy Eureka Server:
```bash
kubectl apply -f k8s/eureka-server-deployment.yaml
```

3. Deploy Config Server:
```bash
kubectl apply -f k8s/config-server-deployment.yaml
```

4. Deploy RabbitMQ:
```bash
kubectl apply -f k8s/rabbitmq-deployment.yaml
```

### Verify Deployment

```bash
# Check pods
kubectl get pods -n bookstore
kubectl get pods -n bookstore-infrastructure

# Check services
kubectl get services -n bookstore
kubectl get services -n bookstore-infrastructure

# Access via NodePort
# Eureka: http://<node-ip>:30761
# Config: http://<node-ip>:30888

# Access RabbitMQ Management UI via port-forward
kubectl port-forward svc/rabbitmq 15672:15672 -n bookstore-infrastructure
# Then access: http://localhost:15672 (bookstore/bookstore123)
```

## Configuration Management

### Service Configurations

The Config Server manages configurations for all microservices:

- `application.yml`: Common configuration for all services
- `user-management-service.yml`: User service specific config
- `book-catalog-service.yml`: Book catalog service specific config
- `loan-management-service.yml`: Loan management service specific config

### Environment-Specific Configs

Add environment-specific configurations by creating files like:
- `application-dev.yml`
- `application-prod.yml`
- `user-management-service-dev.yml`

### Configuration Refresh

Services can refresh their configuration without restart:
```bash
curl -X POST http://service-host:port/actuator/refresh
```

## Monitoring and Health Checks

### Health Endpoints

- Eureka Server: http://localhost:8761/actuator/health
- Config Server: http://localhost:8888/actuator/health
- RabbitMQ: `rabbitmq-diagnostics -q ping`

### Metrics

- Eureka and Config Server expose Prometheus metrics at `/actuator/prometheus`
- RabbitMQ exposes metrics at http://localhost:15672/api/metrics

### Logs

Logs are written to:
- Container: `/app/logs/`
- Docker volumes: `eureka-logs`, `config-logs`, `rabbitmq-logs`

## Security

### Authentication

All services use basic authentication:
- Eureka Server: `eureka/eureka123`
- Config Server: `config/config123`
- RabbitMQ: `bookstore/bookstore123`

### Environment Variables

Set these environment variables for production:
- `EUREKA_PASSWORD`: Password for Eureka Server
- `CONFIG_PASSWORD`: Password for Config Server
- `RABBITMQ_DEFAULT_USER`: RabbitMQ username
- `RABBITMQ_DEFAULT_PASS`: RabbitMQ password

## Troubleshooting

### Common Issues

1. **Service registration fails**
   - Check Eureka Server is running and accessible
   - Verify network connectivity between services
   - Check authentication credentials

2. **Configuration not loading**
   - Verify Config Server is running
   - Check service name matches configuration file name
   - Verify authentication credentials

3. **Health checks failing**
   - Check service startup time (increase initial delay)
   - Verify port accessibility
   - Check application logs

### Debugging

Enable debug logging by setting:
```yaml
logging:
  level:
    com.netflix.eureka: DEBUG
    org.springframework.cloud.config: DEBUG
```

## Next Steps

After infrastructure services are running:

1. Implement API Gateway (Task 2)
2. Set up monitoring infrastructure (Task 3)
3. Deploy individual microservices
4. Configure service-to-service communication