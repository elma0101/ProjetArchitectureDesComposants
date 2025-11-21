#!/bin/bash

# Kubernetes Deployment Script for Bookstore Application
set -e

NAMESPACE=${1:-bookstore}
ENVIRONMENT=${2:-production}

echo "🚀 Deploying to Kubernetes namespace: $NAMESPACE"
echo "🌍 Environment: $ENVIRONMENT"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if we can connect to the cluster
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster"
    exit 1
fi

# Create namespace if it doesn't exist
echo "📁 Creating namespace..."
kubectl apply -f kubernetes/namespace.yaml

# Apply ConfigMap and Secrets
echo "⚙️ Applying configuration..."
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/secret.yaml

# Apply PVCs
echo "💾 Creating persistent volumes..."
kubectl apply -f kubernetes/pvc.yaml

# Deploy applications
echo "🚀 Deploying backend..."
kubectl apply -f kubernetes/backend-deployment.yaml

echo "🌐 Deploying frontend..."
kubectl apply -f kubernetes/frontend-deployment.yaml

# Apply ingress
echo "🌍 Setting up ingress..."
kubectl apply -f kubernetes/ingress.yaml

# Wait for deployments to be ready
echo "⏳ Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/bookstore-backend -n $NAMESPACE
kubectl wait --for=condition=available --timeout=300s deployment/bookstore-frontend -n $NAMESPACE

# Check pod status
echo "📊 Checking pod status..."
kubectl get pods -n $NAMESPACE

# Check services
echo "🔗 Checking services..."
kubectl get services -n $NAMESPACE

# Check ingress
echo "🌍 Checking ingress..."
kubectl get ingress -n $NAMESPACE

echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Useful commands:"
echo "  View pods: kubectl get pods -n $NAMESPACE"
echo "  View logs: kubectl logs -f deployment/bookstore-backend -n $NAMESPACE"
echo "  Port forward: kubectl port-forward service/bookstore-backend-service 8080:8080 -n $NAMESPACE"
echo "  Scale backend: kubectl scale deployment bookstore-backend --replicas=5 -n $NAMESPACE"