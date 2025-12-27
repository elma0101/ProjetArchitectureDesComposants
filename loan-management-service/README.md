# Loan Management Service

Microservice responsible for managing book loans in the bookstore application.

## Overview

The Loan Management Service handles all loan-related operations including:
- Creating new loans
- Processing book returns
- Tracking loan history
- Managing overdue loans
- Coordinating with Book Catalog and User Management services

## Architecture

This service is part of the microservices architecture and:
- Registers with Eureka Service Registry
- Retrieves configuration from Config Server
- Communicates with Book Catalog Service via Feign client
- Communicates with User Management Service via Feign client
- Uses PostgreSQL for data persistence
- Implements circuit breaker patterns for resilience

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **HTTP Client**: OpenFeign
- **Resilience**: Resilience4j Circuit Breaker
- **Database Migration**: Flyway
- **Monitoring**: Spring Boot Actuator + Prometheus

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15
- Docker (optional, for containerized deployment)
- Running Eureka Server (port 8761)
- Running Config Server (port 8888)
- Running Book Catalog Service
- Running User Management Service

## Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE loan_management_db;
CREATE USER bookstore WITH PASSWORD 'bookstore123';
GRANT ALL PRIVILEGES ON DATABASE loan_management_db TO bookstore;
```

Flyway will automatically run migrations on startup.

## Configuration

The service uses Spring Cloud Config Server for centralized configuration. Local configuration is available in:

- `application.yml` - Default configuration
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile

### Key Configuration Properties

```yaml
server:
  port: 8083

spring:
  application:
    name: loan-management-service
  datasource:
    url: jdbc:postgresql://localhost:5432/loan_management_db
    username: bookstore
    password: bookstore123

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Building the Service

### Using Maven

```bash
mvn clean package
```

### Using the Build Script

```bash
chmod +x build.sh
./build.sh
```

This will:
1. Build the application with Maven
2. Create a Docker image

## Running the Service

### Local Development

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Using Docker Compose

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database (port 5435)
- Loan Management Service (port 8083)

### Using Kubernetes

```bash
kubectl apply -f k8s/deployment.yaml
```

## API Endpoints

### Health Check
- `GET /api/health` - Service health status
- `GET /actuator/health` - Detailed health information

### Loan Operations (to be implemented in next tasks)
- `POST /api/loans` - Create a new loan
- `GET /api/loans/{id}` - Get loan details
- `GET /api/loans/user/{userId}` - Get user's loans
- `PUT /api/loans/{id}/return` - Return a book
- `GET /api/loans/overdue` - Get overdue loans

## Service Communication

### Feign Clients

**Book Catalog Service**
- `GET /api/books/{id}` - Get book details
- `GET /api/books/{id}/availability` - Check book availability
- `PUT /api/books/{id}/borrow` - Mark book as borrowed
- `PUT /api/books/{id}/return` - Mark book as returned

**User Management Service**
- `GET /api/users/{id}` - Get user details

### Circuit Breaker

The service implements circuit breaker patterns using Resilience4j:
- Automatic fallback when dependent services are unavailable
- Configurable failure thresholds
- Half-open state for recovery testing

## Database Schema

### Tables

**loans**
- `id` - Primary key
- `user_id` - Reference to user (from User Management Service)
- `book_id` - Reference to book (from Book Catalog Service)
- `loan_date` - Date when book was borrowed
- `due_date` - Date when book should be returned
- `return_date` - Actual return date (nullable)
- `status` - Loan status (ACTIVE, RETURNED, OVERDUE, CANCELLED)
- `created_at` - Record creation timestamp
- `updated_at` - Record update timestamp

**loan_tracking**
- `id` - Primary key
- `loan_id` - Reference to loan
- `status` - Status at this point in time
- `timestamp` - When the status change occurred
- `notes` - Additional notes
- `changed_by` - Who made the change

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Metrics

The service exposes Prometheus metrics for:
- HTTP request rates and latencies
- Database connection pool statistics
- Circuit breaker states
- JVM metrics

## Testing

Run tests with:

```bash
mvn test
```

## Troubleshooting

### Service won't start
- Verify PostgreSQL is running and accessible
- Check Eureka Server is running on port 8761
- Verify database credentials in configuration

### Cannot connect to other services
- Ensure Book Catalog Service is registered with Eureka
- Ensure User Management Service is registered with Eureka
- Check circuit breaker status in actuator endpoints

### Database migration fails
- Check Flyway migration scripts in `src/main/resources/db/migration`
- Verify database user has necessary permissions
- Check for conflicting schema changes

## Development

### Project Structure

```
loan-management-service/
├── src/
│   ├── main/
│   │   ├── java/com/bookstore/loanmanagement/
│   │   │   ├── client/          # Feign clients
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── repository/      # Data repositories
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       └── application.yml  # Configuration
│   └── test/                    # Test files
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Next Steps

This foundation service will be extended with:
- Loan business logic implementation (Task 14)
- Loan management APIs (Task 15)
- Distributed transaction handling with Saga pattern (Task 16)

## License

Copyright © 2024 Bookstore Application
