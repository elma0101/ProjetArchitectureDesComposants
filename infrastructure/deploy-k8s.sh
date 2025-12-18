#!/bin/bash

# Kubernetes deployment script for infrastructure services
set -e

echo "Deploying Infrastructure Services to Kubernetes..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if kubectl can connect to cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

print_status "Connected to Kubernetes cluster"

# Create namespace
print_status "Creating bookstore namespace..."
kubectl apply -f k8s/namespace.yaml

# Create secrets
print_status "Creating infrastructure secrets..."
kubectl apply -f k8s/infrastructure-secrets.yaml

# Deploy Eureka Server
print_status "Deploying Eureka Server..."
kubectl apply -f k8s/eureka-server-deployment.yaml

# Wait for Eureka Server to be ready
print_status "Waiting for Eureka Server to be ready..."
kubectl wait --for=condition=ready pod -l app=eureka-server -n bookstore --timeout=300s

if [ $? -eq 0 ]; then
    print_status "Eureka Server is ready"
else
    print_error "Eureka Server failed to start within timeout"
    kubectl logs -l app=eureka-server -n bookstore --tail=50
    exit 1
fi

# Deploy Config Server
print_status "Deploying Config Server..."
kubectl apply -f k8s/config-server-deployment.yaml

# Wait for Config Server to be ready
print_status "Waiting for Config Server to be ready..."
kubectl wait --for=condition=ready pod -l app=config-server -n bookstore --timeout=300s

if [ $? -eq 0 ]; then
    print_status "Config Server is ready"
else
    print_error "Config Server failed to start within timeout"
    kubectl logs -l app=config-server -n bookstore --tail=50
    exit 1
fi

# Deploy RabbitMQ
print_status "Deploying RabbitMQ cluster..."
kubectl apply -f k8s/rabbitmq-deployment.yaml

# Wait for RabbitMQ to be ready
print_status "Waiting for RabbitMQ cluster to be ready..."
kubectl wait --for=condition=ready pod -l app=rabbitmq -n bookstore-infrastructure --timeout=300s

if [ $? -eq 0 ]; then
    print_status "RabbitMQ cluster is ready"
else
    print_warning "RabbitMQ cluster may still be initializing. Check status with: kubectl get pods -l app=rabbitmq -n bookstore-infrastructure"
fi

print_status "Infrastructure services deployed successfully!"

# Display deployment status
print_status "Deployment Status:"
kubectl get pods -n bookstore
kubectl get services -n bookstore

# Get node IPs for NodePort access
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
if [ -z "$NODE_IP" ]; then
    NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
fi

print_status "Access URLs:"
print_status "Eureka Server: http://$NODE_IP:30761 (eureka/eureka123)"
print_status "Config Server: http://$NODE_IP:30888 (config/config123)"
print_status ""
print_status "To access RabbitMQ Management UI, run:"
print_status "kubectl port-forward svc/rabbitmq 15672:15672 -n bookstore-infrastructure"
print_status "Then access: http://localhost:15672 (bookstore/bookstore123)"

print_status ""
print_status "To check logs:"
print_status "kubectl logs -f deployment/eureka-server -n bookstore"
print_status "kubectl logs -f deployment/config-server -n bookstore"
print_status "kubectl logs -f statefulset/rabbitmq -n bookstore-infrastructure"