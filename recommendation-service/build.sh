#!/bin/bash

echo "Building Recommendation Service..."

# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t bookstore/recommendation-service:latest .

echo "Build complete!"
