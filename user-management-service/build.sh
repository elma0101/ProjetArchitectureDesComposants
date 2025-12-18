#!/bin/bash

# User Management Service Build Script

set -e

echo "Building User Management Service..."

# Clean and build with Maven
echo "Running Maven build..."
./mvnw clean package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t bookstore/user-management-service:latest .

echo "Build completed successfully!"
echo "Docker image: bookstore/user-management-service:latest"
