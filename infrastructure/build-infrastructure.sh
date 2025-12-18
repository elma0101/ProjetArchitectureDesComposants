#!/bin/bash

# Build script for infrastructure services
set -e

echo "Building Infrastructure Services..."

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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build Eureka Server
print_status "Building Eureka Server..."
cd eureka-server

if [ ! -f "mvnw" ]; then
    print_error "Maven wrapper not found in eureka-server directory"
    exit 1
fi

# Make mvnw executable
chmod +x mvnw

# Build the application
print_status "Compiling Eureka Server..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    print_status "Eureka Server build successful"
else
    print_error "Eureka Server build failed"
    exit 1
fi

# Build Docker image
print_status "Building Eureka Server Docker image..."
docker build -t bookstore/eureka-server:latest .

if [ $? -eq 0 ]; then
    print_status "Eureka Server Docker image built successfully"
else
    print_error "Eureka Server Docker image build failed"
    exit 1
fi

cd ..

# Build Config Server
print_status "Building Config Server..."
cd config-server

if [ ! -f "mvnw" ]; then
    print_error "Maven wrapper not found in config-server directory"
    exit 1
fi

# Make mvnw executable
chmod +x mvnw

# Build the application
print_status "Compiling Config Server..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    print_status "Config Server build successful"
else
    print_error "Config Server build failed"
    exit 1
fi

# Build Docker image
print_status "Building Config Server Docker image..."
docker build -t bookstore/config-server:latest .

if [ $? -eq 0 ]; then
    print_status "Config Server Docker image built successfully"
else
    print_error "Config Server Docker image build failed"
    exit 1
fi

cd ..

print_status "All infrastructure services built successfully!"
print_status "Available Docker images:"
docker images | grep bookstore

print_status ""
print_status "To start the infrastructure services, run:"
print_status "docker-compose -f docker-compose.infrastructure.yml up -d"
print_status ""
print_status "Infrastructure Services:"
print_status "  - Eureka Server: http://localhost:8761"
print_status "  - Config Server: http://localhost:8888"
print_status "  - RabbitMQ Management: http://localhost:15672 (user: bookstore, pass: bookstore123)"
print_status "  - RabbitMQ AMQP: amqp://localhost:5672"