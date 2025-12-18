# RabbitMQ Message Broker Infrastructure

This directory contains the configuration for the RabbitMQ message broker used for asynchronous communication between microservices in the bookstore application.

## Overview

RabbitMQ is configured with:
- **Virtual Host**: `/bookstore` - Isolated environment for bookstore services
- **User**: `bookstore` with administrator privileges
- **Management UI**: Available on port 15672
- **AMQP Port**: 5672 for service connections

## Architecture

### Exchanges

1. **bookstore.events** (Topic Exchange)
   - Main exchange for all application events
   - Uses topic-based routing for flexible message distribution

2. **bookstore.dlx** (Dead Letter Exchange)
   - Handles failed messages from all queues
   - Routes to dead-letter queue for analysis

### Queues

#### Book Service Queues
- `book.created` - New book creation events
- `book.updated` - Book update events
- `book.deleted` - Book deletion events
- `book.availability.changed` - Book availability changes

#### Loan Service Queues
- `loan.created` - New loan creation events
- `loan.returned` - Loan return events
- `loan.overdue` - Overdue loan notifications

#### Supporting Service Queues
- `notification.email` - Email notification requests
- `audit.log` - Audit log events
- `recommendation.update` - Recommendation engine updates

#### Dead Letter Queue
- `dead-letter.queue` - Failed messages for manual review

### Routing Keys

The system uses topic-based routing with the following patterns:

- `book.created` - Book creation events
- `book.updated` - Book update events
- `book.deleted` - Book deletion events
- `book.availability.*` - Book availability changes (wildcard)
- `loan.created` - Loan creation events
- `loan.returned` - Loan return events
- `loan.overdue` - Overdue loan events
- `notification.*` - All notification events
- `audit.*` - All audit events
- `recommendation.*` - All recommendation events

## Dead Letter Queue (DLQ) Handling

All queues are configured with automatic dead letter handling:

1. **Automatic DLQ Policy**: Messages that fail processing are automatically routed to the dead letter exchange
2. **TTL**: Dead letter messages are retained for 7 days (604800000 ms)
3. **Manual Review**: Failed messages can be inspected via the management UI
4. **Reprocessing**: Messages can be manually moved back to original queues after fixing issues

### DLQ Configuration

- Dead letter exchange: `bookstore.dlx`
- Dead letter routing key: `dead-letter`
- Dead letter queue: `dead-letter.queue`
- Message TTL: 7 days

## Queue Configuration

All queues have the following settings:

- **Durable**: Yes - Queues survive broker restarts
- **Auto-delete**: No - Queues persist even when no consumers
- **Message TTL**: 24 hours (86400000 ms) for most queues, 7 days for audit and DLQ
- **Max Length**: 10,000 messages (50,000 for audit logs)
- **Dead Letter Exchange**: Configured for automatic failure handling

## High Availability

### Policies

1. **ha-all**: All queues and exchanges are mirrored across all cluster nodes
2. **dlx-policy**: Automatic dead letter exchange configuration for all queues

### Clustering

- Configured for Kubernetes StatefulSet deployment with 3 replicas
- Automatic peer discovery using Kubernetes API
- Partition handling: `autoheal` - Automatically resolves network partitions

## Monitoring

### Health Checks

- **Liveness Probe**: `rabbitmq-diagnostics -q ping`
- **Readiness Probe**: `rabbitmq-diagnostics -q check_running`

### Prometheus Metrics

RabbitMQ exposes metrics at `/api/metrics` on the management port (15672).

Key metrics to monitor:
- Queue depth and message rates
- Consumer count and utilization
- Memory and disk usage
- Connection and channel counts
- Message acknowledgment rates
- Dead letter queue depth

### Management UI

Access the RabbitMQ Management UI:
- **Docker**: http://localhost:15672
- **Kubernetes**: Port-forward or use ingress

Login credentials:
- Username: `bookstore`
- Password: `bookstore123`

## Usage

### Docker Compose

Start RabbitMQ with infrastructure services:

```bash
cd infrastructure
docker-compose -f docker-compose.infrastructure.yml up -d rabbitmq
```

Check logs:
```bash
docker logs rabbitmq -f
```

### Kubernetes

Deploy RabbitMQ cluster:

```bash
kubectl apply -f infrastructure/k8s/rabbitmq-deployment.yaml
```

Check status:
```bash
kubectl get statefulset rabbitmq -n bookstore-infrastructure
kubectl get pods -l app=rabbitmq -n bookstore-infrastructure
```

Port-forward for local access:
```bash
kubectl port-forward svc/rabbitmq 15672:15672 5672:5672 -n bookstore-infrastructure
```

## Connection Configuration

### Spring Boot Services

Add to `application.yml`:

```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: bookstore
    password: bookstore123
    virtual-host: /bookstore
    connection-timeout: 10000
    requested-heartbeat: 60
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2
```

### Environment Variables

For Docker/Kubernetes deployments:

```bash
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=bookstore
SPRING_RABBITMQ_PASSWORD=bookstore123
SPRING_RABBITMQ_VIRTUAL_HOST=/bookstore
```

## Message Publishing Example

```java
@Service
public class BookEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishBookCreated(Book book) {
        rabbitTemplate.convertAndSend(
            "bookstore.events",
            "book.created",
            book
        );
    }
}
```

## Message Consuming Example

```java
@Service
public class BookEventConsumer {
    
    @RabbitListener(queues = "book.created")
    public void handleBookCreated(Book book) {
        // Process book creation event
        log.info("Received book created event: {}", book.getId());
    }
}
```

## Troubleshooting

### Check RabbitMQ Status

```bash
# Docker
docker exec rabbitmq rabbitmq-diagnostics status

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmq-diagnostics status
```

### View Queue Status

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_queues

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_queues
```

### Check Connections

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_connections

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_connections
```

### Purge Queue (Development Only)

```bash
# Docker
docker exec rabbitmq rabbitmqctl purge_queue book.created

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl purge_queue book.created
```

## Security Considerations

1. **Change Default Password**: Update the default password in production
2. **TLS/SSL**: Enable TLS for production deployments
3. **Network Policies**: Restrict access to RabbitMQ ports
4. **Secrets Management**: Use Kubernetes secrets or vault for credentials
5. **Virtual Host Isolation**: Each environment should use separate virtual hosts

## Performance Tuning

### Memory Management

- Default memory high watermark: 60% of available memory
- Disk free limit: 2GB minimum

### Connection Limits

- Max channels per connection: 2047
- Max frame size: 131072 bytes
- Heartbeat interval: 60 seconds

### Queue Optimization

- Use lazy queues for large message volumes
- Configure appropriate message TTL
- Set max-length to prevent unbounded growth
- Enable message persistence for critical queues

## Backup and Recovery

### Export Definitions

```bash
# Docker
docker exec rabbitmq rabbitmqctl export_definitions /tmp/definitions.json

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl export_definitions /tmp/definitions.json
```

### Import Definitions

```bash
# Docker
docker exec rabbitmq rabbitmqctl import_definitions /tmp/definitions.json

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl import_definitions /tmp/definitions.json
```

## References

- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Management Plugin](https://www.rabbitmq.com/management.html)
- [RabbitMQ Monitoring](https://www.rabbitmq.com/monitoring.html)
