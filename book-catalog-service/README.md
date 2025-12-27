# Book Catalog Service

Microservice responsible for managing books and authors in the bookstore application.

## Features

- Book management (CRUD operations)
- Author management (CRUD operations)
- Book-Author relationship management
- Book availability tracking
- Service discovery with Eureka
- Centralized configuration with Spring Cloud Config
- Service-to-service communication with Feign
- Circuit breaker pattern with Resilience4j
- Database migration with Flyway

## Technology Stack

- Java 17
- Spring Boot 3.1.5
- Spring Cloud 2022.0.4
- PostgreSQL
- Flyway
- Lombok
- Spring Data JPA

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- Eureka Server running on port 8761
- Config Server running on port 8888

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE book_catalog_db;
CREATE USER bookstore WITH PASSWORD 'bookstore123';
GRANT ALL PRIVILEGES ON DATABASE book_catalog_db TO bookstore;
```

## Running the Service

### Local Development

```bash
# Build the project
mvn clean install

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
# Build Docker image
docker build -t book-catalog-service:latest .

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/book_catalog_db \
  -e DATABASE_USERNAME=bookstore \
  -e DATABASE_PASSWORD=bookstore123 \
  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
  book-catalog-service:latest
```

## API Endpoints

### Health Check
- `GET /api/health` - Service health status

### Actuator Endpoints
- `GET /actuator/health` - Detailed health information
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Service metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Configuration

The service uses Spring Cloud Config for centralized configuration. Configuration files are located in the Config Server repository.

### Key Configuration Properties

- `server.port`: 8082
- `spring.application.name`: book-catalog-service
- `eureka.client.service-url.defaultZone`: Eureka server URL
- `spring.datasource.url`: PostgreSQL connection URL

## Service Communication

The service communicates with:
- **User Management Service**: For user validation and authentication

## Database Schema

### Tables
- `books`: Book information
- `authors`: Author information
- `book_authors`: Many-to-many relationship between books and authors

## Monitoring

The service exposes Prometheus metrics at `/actuator/prometheus` for monitoring and alerting.

## Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify
```

## Development

### Adding New Features

1. Create entity classes in `entity` package
2. Create repository interfaces in `repository` package
3. Implement service logic in `service` package
4. Create REST controllers in `controller` package
5. Add database migrations in `src/main/resources/db/migration`

### Code Style

The project uses Lombok to reduce boilerplate code. Make sure your IDE has Lombok plugin installed.

## Troubleshooting

### Service not registering with Eureka
- Check if Eureka server is running
- Verify `eureka.client.service-url.defaultZone` configuration
- Check network connectivity

### Database connection issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database exists and user has proper permissions

### Flyway migration errors
- Check migration scripts for syntax errors
- Verify database schema version
- Use `flyway:repair` if needed

## License

Copyright © 2024 Bookstore Application
