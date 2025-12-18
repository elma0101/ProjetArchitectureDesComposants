#!/bin/bash

# Deploy Monitoring Stack Script

set -e

echo "Deploying Bookstore Monitoring Stack..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running"
    exit 1
fi

# Create monitoring network if it doesn't exist
docker network inspect monitoring >/dev/null 2>&1 || docker network create monitoring

# Start monitoring services
echo "Starting monitoring services..."
docker-compose -f docker-compose.monitoring.yml up -d

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 30

# Check Prometheus
echo "Checking Prometheus..."
if curl -f http://localhost:9090/-/healthy > /dev/null 2>&1; then
    echo "✓ Prometheus is healthy"
else
    echo "✗ Prometheus is not healthy"
fi

# Check Grafana
echo "Checking Grafana..."
if curl -f http://localhost:3000/api/health > /dev/null 2>&1; then
    echo "✓ Grafana is healthy"
else
    echo "✗ Grafana is not healthy"
fi

# Check Elasticsearch
echo "Checking Elasticsearch..."
if curl -f http://localhost:9200/_cluster/health > /dev/null 2>&1; then
    echo "✓ Elasticsearch is healthy"
else
    echo "✗ Elasticsearch is not healthy"
fi

# Check Kibana
echo "Checking Kibana..."
if curl -f http://localhost:5601/api/status > /dev/null 2>&1; then
    echo "✓ Kibana is healthy"
else
    echo "✗ Kibana is not healthy"
fi

# Check Jaeger
echo "Checking Jaeger..."
if curl -f http://localhost:16686/ > /dev/null 2>&1; then
    echo "✓ Jaeger is healthy"
else
    echo "✗ Jaeger is not healthy"
fi

echo ""
echo "Monitoring Stack Deployment Complete!"
echo ""
echo "Access URLs:"
echo "  Prometheus:    http://localhost:9090"
echo "  Grafana:       http://localhost:3000 (admin/admin)"
echo "  Alertmanager:  http://localhost:9093"
echo "  Kibana:        http://localhost:5601"
echo "  Jaeger:        http://localhost:16686"
echo "  Elasticsearch: http://localhost:9200"
echo ""
echo "To view logs: docker-compose -f docker-compose.monitoring.yml logs -f"
echo "To stop:      docker-compose -f docker-compose.monitoring.yml down"