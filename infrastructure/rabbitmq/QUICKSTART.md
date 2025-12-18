# RabbitMQ Quick Start Guide

## Starting RabbitMQ

### Docker Compose (Recommended for Development)

```bash
# Start all infrastructure services including RabbitMQ
docker-compose -f infrastructure/docker-compose.infrastructure.yml up -d

# Start only RabbitMQ
docker-compose -f infrastructure/docker-compose.infrastructure.yml up -d rabbitmq

# Check RabbitMQ status
docker-compose -f infrastructure/docker-compose.infrastructure.yml ps rabbitmq

# View RabbitMQ logs
docker-compose -f infrastructure/docker-compose.infrastructure.yml logs -f rabbitmq
```

### Kubernetes

```bash
# Deploy RabbitMQ cluster
kubectl apply -f infrastructure/k8s/rabbitmq-deployment.yaml

# Check deployment status
kubectl get statefulset rabbitmq -n bookstore-infrastructure
kubectl get pods -l app=rabbitmq -n bookstore-infrastructure

# Access Management UI via port-forward
kubectl port-forward svc/rabbitmq 15672:15672 5672:5672 -n bookstore-infrastructure
```

## Accessing RabbitMQ

### Management UI

- **URL**: http://localhost:15672
- **Username**: bookstore
- **Password**: bookstore123

### AMQP Connection

- **Host**: localhost (or rabbitmq in Docker network)
- **Port**: 5672
- **Virtual Host**: /bookstore
- **Username**: bookstore
- **Password**: bookstore123

## Verifying Setup

### Check Health

```bash
# Docker
docker exec rabbitmq rabbitmq-diagnostics -q ping

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmq-diagnostics -q ping
```

### List Queues

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_queues

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_queues
```

Expected output should show all configured queues:
- book.created
- book.updated
- book.deleted
- book.availability.changed
- loan.created
- loan.returned
- loan.overdue
- notification.email
- audit.log
- recommendation.update
- dead-letter.queue

### List Exchanges

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_exchanges

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_exchanges
```

Expected output should include:
- bookstore.events (topic)
- bookstore.dlx (topic)

## Testing Message Flow

### Publish a Test Message

Using the Management UI:
1. Navigate to http://localhost:15672
2. Go to "Queues" tab
3. Click on a queue (e.g., "book.created")
4. Expand "Publish message"
5. Enter test payload and click "Publish message"

### Using rabbitmqadmin (CLI)

```bash
# Install rabbitmqadmin
docker exec rabbitmq wget http://localhost:15672/cli/rabbitmqadmin
docker exec rabbitmq chmod +x rabbitmqadmin

# Publish a message
docker exec rabbitmq ./rabbitmqadmin publish exchange=bookstore.events routing_key=book.created payload='{"id":1,"title":"Test Book"}'

# Get messages from queue
docker exec rabbitmq ./rabbitmqadmin get queue=book.created
```

## Common Operations

### Purge a Queue

```bash
# Docker
docker exec rabbitmq rabbitmqctl purge_queue book.created

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl purge_queue book.created
```

### Check Connection Count

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_connections

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_connections
```

### View Queue Details

```bash
# Docker
docker exec rabbitmq rabbitmqctl list_queues name messages consumers

# Kubernetes
kubectl exec -it rabbitmq-0 -n bookstore-infrastructure -- rabbitmqctl list_queues name messages consumers
```

## Monitoring

### Prometheus Metrics

RabbitMQ exposes Prometheus metrics at:
- http://localhost:15672/api/metrics

Add to your Prometheus configuration:
```yaml
scrape_configs:
  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15672']
    metrics_path: '/api/metrics'
    basic_auth:
      username: 'bookstore'
      password: 'bookstore123'
```

### Key Metrics to Monitor

- `rabbitmq_queue_messages` - Number of messages in queue
- `rabbitmq_queue_messages_unacked` - Unacknowledged messages
- `rabbitmq_queue_consumers` - Number of consumers
- `rabbitmq_node_mem_used` - Memory usage
- `rabbitmq_node_disk_free` - Free disk space
- `rabbitmq_connections` - Active connections
- `rabbitmq_channel_messages_published_total` - Published messages

## Troubleshooting

### RabbitMQ Won't Start

Check logs:
```bash
docker logs rabbitmq
```

Common issues:
- Port 5672 or 15672 already in use
- Insufficient memory or disk space
- Configuration file syntax errors

### Cannot Connect to RabbitMQ

1. Check if RabbitMQ is running:
   ```bash
   docker ps | grep rabbitmq
   ```

2. Check network connectivity:
   ```bash
   docker exec rabbitmq rabbitmq-diagnostics ping
   ```

3. Verify credentials in your application configuration

### Messages Not Being Consumed

1. Check if consumers are connected:
   ```bash
   docker exec rabbitmq rabbitmqctl list_consumers
   ```

2. Check queue bindings:
   ```bash
   docker exec rabbitmq rabbitmqctl list_bindings
   ```

3. Check for errors in dead letter queue:
   - Navigate to Management UI
   - Check "dead-letter.queue" for failed messages

### High Memory Usage

1. Check memory usage:
   ```bash
   docker exec rabbitmq rabbitmq-diagnostics memory_breakdown
   ```

2. Purge unnecessary queues
3. Adjust memory limits in configuration
4. Enable lazy queues for large message volumes

## Next Steps

1. **Configure Spring Boot Services**: Add RabbitMQ dependencies and configuration
2. **Implement Message Publishers**: Create services to publish events
3. **Implement Message Consumers**: Create listeners to process events
4. **Set Up Monitoring**: Configure Prometheus and Grafana dashboards
5. **Test Message Flow**: Verify end-to-end message processing

## Resources

- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP Reference](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Management Plugin](https://www.rabbitmq.com/management.html)
- [RabbitMQ Best Practices](https://www.rabbitmq.com/best-practices.html)
