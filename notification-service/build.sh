#!/bin/bash

# Build script for Notification Service

echo "Building Notification Service..."

# Clean and build with Maven
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Build Docker image
    echo "Building Docker image..."
    docker build -t bookstore/notification-service:latest .
    
    if [ $? -eq 0 ]; then
        echo "Docker image built successfully!"
    else
        echo "Docker image build failed!"
        exit 1
    fi
else
    echo "Maven build failed!"
    exit 1
fi
