#!/bin/bash

# Bookstore Application Deployment Script
set -e

# Configuration
ENVIRONMENT=${1:-production}
VERSION=${2:-latest}
COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"

echo "🚀 Starting deployment for environment: $ENVIRONMENT"
echo "📦 Version: $VERSION"

# Check if required files exist
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "❌ Error: $COMPOSE_FILE not found"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "⚠️  Warning: $ENV_FILE not found, using .env.example"
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "📝 Please update .env file with your configuration"
    else
        echo "❌ Error: Neither .env nor .env.example found"
        exit 1
    fi
fi

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check required dependencies
echo "🔍 Checking dependencies..."
if ! command_exists docker; then
    echo "❌ Error: Docker is not installed"
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Error: Docker Compose is not installed"
    exit 1
fi

# Check Docker daemon
if ! docker info >/dev/null 2>&1; then
    echo "❌ Error: Docker daemon is not running"
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs uploads backups

# Pull latest images
echo "📥 Pulling latest images..."
docker-compose pull

# Build application images
echo "🔨 Building application images..."
docker-compose build --no-cache

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose down --remove-orphans

# Start services
echo "🚀 Starting services..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
timeout=300
counter=0

while [ $counter -lt $timeout ]; do
    if docker-compose ps | grep -q "Up (healthy)"; then
        echo "✅ Services are healthy"
        break
    fi
    
    if [ $counter -eq $timeout ]; then
        echo "❌ Timeout waiting for services to be healthy"
        docker-compose logs
        exit 1
    fi
    
    echo "⏳ Waiting... ($counter/$timeout seconds)"
    sleep 10
    counter=$((counter + 10))
done

# Run health checks
echo "🏥 Running health checks..."
sleep 30

# Check backend health
if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✅ Backend is healthy"
else
    echo "❌ Backend health check failed"
    docker-compose logs backend
    exit 1
fi

# Check frontend health
if curl -f http://localhost:3000/health >/dev/null 2>&1; then
    echo "✅ Frontend is healthy"
else
    echo "❌ Frontend health check failed"
    docker-compose logs frontend
    exit 1
fi

# Display running services
echo "📊 Running services:"
docker-compose ps

echo "🎉 Deployment completed successfully!"
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Backend API: http://localhost:8080/api"
echo "📊 Health Check: http://localhost:8080/actuator/health"
echo "📈 Metrics: http://localhost:8080/actuator/prometheus"

# Optional: Run smoke tests
if [ "$3" = "--smoke-tests" ]; then
    echo "🧪 Running smoke tests..."
    ./scripts/smoke-tests.sh
fi