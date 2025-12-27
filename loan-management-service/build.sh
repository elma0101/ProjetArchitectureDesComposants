#!/bin/bash

# Build script for Loan Management Service

set -e

echo "Building Loan Management Service..."

# Clean and build with Maven
echo "Running Maven build..."
mvn clean package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t bookstore/loan-management-service:latest .

echo "Build completed successfully!"
echo "To run the service, use: docker-compose up -d"
