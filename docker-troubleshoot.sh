#!/bin/bash

echo "🔧 Docker Troubleshooting Script for Bookstore Application"
echo "========================================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."
if ! command_exists docker; then
    echo "❌ Docker is not installed"
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed"
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Check Docker daemon
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker daemon is not running"
    echo "💡 Start Docker daemon and try again"
    exit 1
fi

echo "✅ Docker daemon is running"

# Clean up previous containers and volumes
echo "🧹 Cleaning up previous containers and volumes..."
docker-compose down -v --remove-orphans
docker system prune -f

# Check .env file
if [ ! -f ".env" ]; then
    echo "⚠️  .env file not found, copying from .env.example"
    cp .env.example .env
fi

# Generate package-lock.json if missing
if [ ! -f "frontend/package-lock.json" ]; then
    echo "📦 Generating package-lock.json..."
    cd frontend
    npm install
    cd ..
fi

# Build images step by step
echo "🏗️  Building images step by step..."

# Build backend first
echo "🔨 Building backend..."
docker-compose build backend
if [ $? -ne 0 ]; then
    echo "❌ Backend build failed"
    echo "💡 Try: cd backend && mvn clean package -DskipTests"
    exit 1
fi

# Build frontend
echo "🔨 Building frontend..."
docker-compose build frontend
if [ $? -ne 0 ]; then
    echo "❌ Frontend build failed"
    echo "💡 Check frontend/package.json and run: cd frontend && npm install"
    exit 1
fi

# Start services one by one
echo "🚀 Starting services..."

# Start databases first
echo "🗄️  Starting databases..."
docker-compose up -d postgres redis

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 30

# Check database health
echo "🏥 Checking database health..."
docker-compose ps

# Start backend
echo "🔧 Starting backend..."
docker-compose up -d backend

# Wait for backend to be ready
echo "⏳ Waiting for backend to be ready..."
sleep 60

# Start frontend
echo "🎨 Starting frontend..."
docker-compose up -d frontend

# Final status check
echo "📊 Final status check..."
docker-compose ps

echo "✅ Setup complete!"
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "📊 Backend Health: http://localhost:8080/actuator/health"

# Show logs if any service is not running
if docker-compose ps | grep -q "Exit"; then
    echo "⚠️  Some services failed to start. Showing logs:"
    docker-compose logs --tail=50
fi