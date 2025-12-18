# Bookstore Microservices Monitoring and Observability

This directory contains the complete monitoring and observability stack for the Bookstore microservices architecture.

## Components

### 1. Prometheus
- **Purpose**: Metrics collection and storage
- **Port**: 9090
- **URL**: http://localhost:9090

Prometheus scrapes metrics from all microservices and infrastructure components. It evaluates alerting rules and sends alerts to Alertmanager.

### 2. Grafana
- **Purpose**: Metrics visualization and dashboarding
- **Port**: 3000
- **URL**: http://localhost:3000
- **Default Credentials**: admin/admin

Grafana provides pre-configured dashboards for:
- Service health and performance
- JVM metrics
- Database connection pools
- API Gateway metrics
- Circuit breaker status
- Business metrics

### 3. Alertmanager
- **Purpose**: Alert routing and notification
- **Port**: 9093
- **URL**: http://localhost:9093

Alertmanager handles alerts from Prometheus and routes them to appropriate channels (email, Slack, PagerDuty).

### 4. ELK Stack

#### Elasticsearch
- **Purpose**: Log storage and search
- **Ports**: 9200 (HTTP), 9300 (Transport)
- **URL**: http://localhost:9200

#### Logstash
- **Purpose**: Log processing and forwarding
- **Ports**: 5000 (TCP/UDP), 9600 (API)

#### Kibana
- **Purpose**: Log visualization and analysis
- **Port**: 5601
- **URL**: http://localhost:5601

### 5. Jaeger
- **Purpose**: Distributed tracing
- **Port**: 16686 (UI)
- **URL**: http://localhost:16686

Jaeger provides end-to-end distributed tracing for requests across microservices.

### 6. Node Exporter
- **Purpose**: System metrics collection
- **Port**: 9100

Collects hardware and OS metrics from the host system.

## Quick Start

### Start All Monitoring Services

```bash
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

### Stop All Monitoring Services

```bash
docker-compose -f docker-compose.monitoring.yml down
```

### View Logs

```bash
# All services
docker-compose -f docker-compose.monitoring.yml logs -f

# Specific service
docker-compose -f docker-compose.monitoring.yml logs -f prometheus
```

## Configuration

### Prometheus Configuration

Edit `prometheus.yml` to:
- Add new scrape targets
- Modify scrape intervals
- Configure service discovery

### Alert Rules

Edit `alert_rules.yml` to:
- Add new alerting rules
- Modify thresholds
- Configure alert labels and annotations

### Alertmanager Configuration

Edit `alertmanager.yml` to:
- Configure notification channels
- Set up routing rules
- Define inhibition rules

**Environment Variables Required:**
- `SMTP_PASSWORD`: SMTP server password for email notifications
- `SLACK_WEBHOOK_URL`: Slack webhook URL for Slack notifications
- `PAGERDUTY_SERVICE_KEY`: PagerDuty service key for critical alerts

### Logstash Configuration

Edit `logstash/pipeline/logstash.conf` to:
- Add new input sources
- Modify log parsing patterns
- Configure output destinations

## Accessing Services

### Prometheus
1. Open http://localhost:9090
2. Use PromQL to query metrics
3. View targets status at http://localhost:9090/targets
4. View alerts at http://localhost:9090/alerts

### Grafana
1. Open http://localhost:3000
2. Login with admin/admin
3. Navigate to Dashboards
4. Import or create custom dashboards

### Kibana
1. Open http://localhost:5601
2. Create index pattern: `bookstore-logs-*`
3. Use Discover to search logs
4. Create visualizations and dashboards

### Jaeger
1. Open http://localhost:16686
2. Select service from dropdown
3. Search for traces
4. Analyze trace details and dependencies

## Metrics Endpoints

All microservices expose metrics at:
```
http://<service-host>:<service-port>/actuator/prometheus
```

Example:
- API Gateway: http://localhost:8080/actuator/prometheus
- User Service: http://localhost:8081/actuator/prometheus

## Alert Types

### Critical Alerts
- ServiceDown: Service is not responding
- DatabaseConnectionPoolExhausted: Database connections exhausted
- CircuitBreakerOpen: Circuit breaker is open
- DeadLetterQueueGrowing: Messages accumulating in DLQ

### Warning Alerts
- HighErrorRate: Elevated error rate
- HighResponseTime: Slow response times
- HighCPUUsage: High CPU utilization
- HighMemoryUsage: High memory utilization
- SlowDatabaseQueries: Database queries taking too long

## Log Shipping

### From Microservices

Configure Logback in each microservice to send logs to Logstash:

```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service":"${spring.application.name}"}</customFields>
    </encoder>
</appender>
```

### Log Format

Logs should include:
- Timestamp
- Log level
- Service name
- Correlation ID
- Thread name
- Logger name
- Message
- Exception (if any)

## Distributed Tracing

### Enable Tracing in Microservices

Add dependencies:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

Configure in application.yml:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://jaeger:9411/api/v2/spans
```

## Best Practices

### Metrics
1. Use consistent naming conventions
2. Add appropriate labels for filtering
3. Monitor both technical and business metrics
4. Set up SLO/SLI tracking

### Logging
1. Use structured logging (JSON format)
2. Include correlation IDs in all logs
3. Log at appropriate levels
4. Avoid logging sensitive information

### Tracing
1. Propagate trace context across services
2. Add custom spans for important operations
3. Include relevant tags and annotations
4. Monitor trace sampling rate

### Alerting
1. Alert on symptoms, not causes
2. Set appropriate thresholds
3. Avoid alert fatigue
4. Document runbooks for each alert

## Troubleshooting

### Prometheus Not Scraping Targets
1. Check service is running and healthy
2. Verify network connectivity
3. Check metrics endpoint is accessible
4. Review Prometheus logs

### Logs Not Appearing in Kibana
1. Verify Logstash is receiving logs
2. Check Elasticsearch cluster health
3. Verify index pattern in Kibana
4. Review Logstash pipeline configuration

### Traces Not Showing in Jaeger
1. Verify Jaeger collector is running
2. Check trace sampling configuration
3. Verify network connectivity
4. Review service tracing configuration

### High Resource Usage
1. Adjust retention periods
2. Reduce scrape intervals
3. Optimize queries and dashboards
4. Scale infrastructure components

## Maintenance

### Data Retention

**Prometheus:**
- Default: 15 days
- Configure with `--storage.tsdb.retention.time` flag

**Elasticsearch:**
- Use Index Lifecycle Management (ILM)
- Configure retention policies
- Archive old indices

**Jaeger:**
- Configure trace retention in storage backend
- Use sampling to reduce volume

### Backup

**Prometheus:**
```bash
# Snapshot
curl -XPOST http://localhost:9090/api/v1/admin/tsdb/snapshot
```

**Elasticsearch:**
```bash
# Create snapshot repository
curl -X PUT "localhost:9200/_snapshot/backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/backup"
  }
}'

# Create snapshot
curl -X PUT "localhost:9200/_snapshot/backup/snapshot_1"
```

### Scaling

**Prometheus:**
- Use federation for multi-cluster setups
- Implement remote storage for long-term retention

**Elasticsearch:**
- Add more nodes to the cluster
- Use index sharding
- Implement hot-warm-cold architecture

**Jaeger:**
- Use Elasticsearch or Cassandra as storage backend
- Scale collector and query components independently

## Security

### Authentication
- Enable authentication in Grafana
- Secure Prometheus with basic auth or OAuth
- Protect Kibana with authentication

### Network Security
- Use TLS for all communications
- Implement network policies
- Restrict access to monitoring ports

### Data Security
- Encrypt data at rest
- Encrypt data in transit
- Implement access controls

## Integration with CI/CD

### Automated Deployment
```bash
# Deploy monitoring stack
kubectl apply -f k8s/monitoring/

# Verify deployment
kubectl get pods -n monitoring
```

### Health Checks
```bash
# Check Prometheus
curl http://prometheus:9090/-/healthy

# Check Elasticsearch
curl http://elasticsearch:9200/_cluster/health
```

## Support

For issues or questions:
1. Check service logs
2. Review configuration files
3. Consult documentation
4. Contact DevOps team