# User Management Service

The User Management Service is a microservice responsible for handling user authentication, authorization, and user profile management in the bookstore application.

## Features

- User registration and authentication
- JWT token generation and validation
- Role-based access control (RBAC)
- User profile management
- Service discovery with Eureka
- Centralized configuration with Spring Cloud Config
- Health checks and monitoring with Actuator
- PostgreSQL database with Flyway migrations

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Migration**: Flyway
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Security**: Spring Security + JWT
- **Monitoring**: Spring Boot Actuator + Prometheus

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL 15 (or use Docker Compose)

## Getting Started

### Local Development

1. **Start the database**:
   ```bash
   docker-compose up -d user-management-db
   ```

2. **Run the application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Access the service**:
   - API: http://localhost:8081
   - Health: http://localhost:8081/actuator/health
   - Metrics: http://localhost:8081/actuator/metrics

### Docker Deployment

1. **Build the Docker image**:
   ```bash
   ./build.sh
   ```

2. **Run with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

### Kubernetes Deployment

1. **Apply the Kubernetes manifests**:
   ```bash
   kubectl apply -f k8s/deployment.yaml
   ```

2. **Verify the deployment**:
   ```bash
   kubectl get pods -n bookstore -l app=user-management-service
   ```

## API Endpoints

### Health Check
- `GET /api/health` - Service health status

### Authentication (Coming in next task)
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh JWT token

### User Management (Coming in next task)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user profile
- `GET /api/users` - List users (admin only)
- `DELETE /api/users/{id}` - Delete user (admin only)

## Configuration

The service can be configured through:

1. **application.yml** - Default configuration
2. **application-{profile}.yml** - Profile-specific configuration
3. **Config Server** - Centralized configuration (production)
4. **Environment Variables** - Runtime configuration

### Key Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Service port | 8081 |
| `spring.datasource.url` | Database URL | jdbc:postgresql://localhost:5432/user_management_db |
| `eureka.client.service-url.defaultZone` | Eureka server URL | http://localhost:8761/eureka/ |
| `jwt.secret` | JWT signing secret | (set via env var) |
| `jwt.expiration` | JWT expiration time (ms) | 86400000 (24 hours) |

## Database Schema

The service uses Flyway for database migrations. The schema includes:

- **users** - User accounts and profiles
- **user_roles** - User role assignments

### Initial Data

The service comes with three default users:
- **admin** / admin123 (ADMIN role)
- **librarian** / librarian123 (LIBRARIAN role)
- **user** / user123 (USER role)

## Monitoring

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Full Health**: `/actuator/health`

### Metrics

Prometheus metrics are exposed at `/actuator/prometheus`

## Development

### Running Tests

```bash
./mvnw test
```

### Building

```bash
./mvnw clean package
```

### Code Style

The project follows standard Spring Boot conventions and uses Lombok for reducing boilerplate code.

## Migration from Monolith

This service extracts user management functionality from the monolithic bookstore application. The migration includes:

1. User and Role entities
2. User repository
3. Database schema and initial data
4. Service registration with Eureka
5. Configuration management

## Next Steps

- Implement authentication endpoints (Task 6)
- Implement user management APIs (Task 7)
- Update API Gateway routing (Task 8)

## Troubleshooting

### Database Connection Issues

If you encounter database connection issues:

1. Verify PostgreSQL is running:
   ```bash
   docker-compose ps user-management-db
   ```

2. Check database logs:
   ```bash
   docker-compose logs user-management-db
   ```

### Service Registration Issues

If the service doesn't register with Eureka:

1. Verify Eureka server is running
2. Check the Eureka URL in configuration
3. Review service logs for connection errors

## License

Copyright © 2024 Bookstore Application
