# Notification Service

The Notification Service is a microservice responsible for managing and sending notifications to users in the bookstore application. It supports multiple notification types (email, SMS, push notifications) and provides template management for consistent messaging.

## Features

- **Multi-channel Notifications**: Support for email, SMS, push, and in-app notifications
- **Template Management**: Create and manage reusable notification templates
- **Event-Driven**: Listens to loan-related events via RabbitMQ
- **Retry Mechanism**: Automatic retry for failed notifications
- **Tracking**: Complete notification history and status tracking
- **Asynchronous Processing**: Non-blocking notification sending

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Database**: PostgreSQL
- **Message Broker**: RabbitMQ
- **Email**: Spring Mail with SMTP
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Template Engine**: Thymeleaf

## API Endpoints

### Notification Management

- `POST /api/notifications/send` - Send a notification
- `GET /api/notifications/user/{userId}` - Get user notifications (paginated)
- `GET /api/notifications/{id}` - Get notification by ID
- `POST /api/notifications/retry` - Retry failed notifications

### Template Management

- `POST /api/notifications/templates` - Create a template
- `PUT /api/notifications/templates/{id}` - Update a template
- `GET /api/notifications/templates/{id}` - Get template by ID
- `GET /api/notifications/templates/name/{name}` - Get template by name
- `GET /api/notifications/templates` - Get all templates
- `GET /api/notifications/templates/type/{type}` - Get templates by type
- `DELETE /api/notifications/templates/{id}` - Delete a template

### Health Check

- `GET /api/health` - Service health check

## Event Listeners

The service listens to the following RabbitMQ queues:

- `notification.loan.created` - Loan creation notifications
- `notification.loan.returned` - Loan return notifications
- `notification.loan.overdue` - Overdue loan reminders
- `notification.loan.due-soon` - Due soon reminders

## Configuration

### Environment Variables

- `SPRING_DATASOURCE_URL` - PostgreSQL database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_RABBITMQ_HOST` - RabbitMQ host
- `SPRING_RABBITMQ_PORT` - RabbitMQ port
- `SPRING_RABBITMQ_USERNAME` - RabbitMQ username
- `SPRING_RABBITMQ_PASSWORD` - RabbitMQ password
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - Eureka server URL

### Email Configuration

Configure your SMTP settings in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## Running the Service

### Local Development

```bash
# Build the service
./build.sh

# Run with Maven
mvn spring-boot:run

# Or run the JAR
java -jar target/notification-service-1.0.0.jar
```

### Docker

```bash
# Build Docker image
docker build -t bookstore/notification-service:latest .

# Run with Docker Compose
docker-compose up -d
```

### Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml
```

## Database Schema

### notifications

- `id` - Primary key
- `user_id` - User identifier
- `type` - Notification type (EMAIL, SMS, PUSH, IN_APP)
- `recipient` - Recipient address
- `subject` - Notification subject
- `content` - Notification content
- `status` - Status (PENDING, SENT, FAILED, RETRYING)
- `error_message` - Error message if failed
- `retry_count` - Number of retry attempts
- `template_id` - Associated template ID
- `created_at` - Creation timestamp
- `sent_at` - Sent timestamp

### notification_templates

- `id` - Primary key
- `name` - Template name (unique)
- `type` - Notification type
- `subject` - Template subject
- `content` - Template content with variables
- `description` - Template description
- `active` - Active status
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

## Template Variables

Templates support variable substitution using `{{variableName}}` syntax:

```
Dear {{userName}},

Your loan for {{bookTitle}} is due on {{dueDate}}.

Thank you!
```

## Monitoring

The service exposes Actuator endpoints for monitoring:

- `/actuator/health` - Health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Service metrics
- `/actuator/prometheus` - Prometheus metrics

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=NotificationServiceTest
```

## Dependencies

Key dependencies include:

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Mail
- Spring Boot Starter AMQP
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- PostgreSQL Driver
- Thymeleaf
- Flyway

## Port

Default port: **8086**

## Service Registration

The service registers with Eureka as `notification-service`.
