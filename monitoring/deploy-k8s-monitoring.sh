#!/bin/bash

# Deploy Monitoring Stack to Kubernetes

set -e

echo "Deploying Monitoring Stack to Kubernetes..."

# Create monitoring namespace
echo "Creating monitoring namespace..."
kubectl apply -f k8s/namespace.yaml

# Deploy Prometheus
echo "Deploying Prometheus..."
kubectl apply -f k8s/prometheus-deployment.yaml

# Deploy Grafana
echo "Deploying Grafana..."
kubectl apply -f k8s/grafana-deployment.yaml

# Wait for deployments to be ready
echo "Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/prometheus -n monitoring
kubectl wait --for=condition=available --timeout=300s deployment/grafana -n monitoring

# Get service URLs
echo ""
echo "Monitoring Stack Deployed Successfully!"
echo ""
echo "To access services:"
echo ""
echo "Prometheus:"
echo "  kubectl port-forward -n monitoring svc/prometheus 9090:9090"
echo "  Then open: http://localhost:9090"
echo ""
echo "Grafana:"
echo "  kubectl port-forward -n monitoring svc/grafana 3000:3000"
echo "  Then open: http://localhost:3000 (admin/admin)"
echo ""
echo "To view pods:"
echo "  kubectl get pods -n monitoring"
echo ""
echo "To view logs:"
echo "  kubectl logs -n monitoring -l app=prometheus"
echo "  kubectl logs -n monitoring -l app=grafana"