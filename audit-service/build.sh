#!/bin/bash

echo "Building Audit Service..."

# Clean and build the project
mvn clean package -DskipTests

# Build Docker image
docker build -t bookstore/audit-service:latest .

echo "Build completed successfully!"
