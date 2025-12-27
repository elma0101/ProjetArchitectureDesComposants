#!/bin/bash

echo "Building Book Catalog Service..."

# Clean and build
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Build Docker image
    echo "Building Docker image..."
    docker build -t book-catalog-service:latest .
    
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
