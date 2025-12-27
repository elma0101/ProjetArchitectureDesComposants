# Book Catalog Service - Event Publishing

## Overview

The Book Catalog Service publishes events to RabbitMQ for all book-related operations. This enables other microservices to react to changes in the book catalog asynchronously.

## Event Types

### 1. BookCreatedEvent
Published when a new book is created in the catalog.

**Routing Key:** `book.created`

**Payload:**
```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "timestamp": "2024-11-29T10:30:00",
  "eventType": "BOOK_CREATED",
  "bookId": 1,
  "isbn": "978-0-123456-78-9",
  "title": "Book Title",
  "description": "Book description",
  "publicationYear": 2024,
  "genre": "Fiction",
  "totalCopies": 10,
  "availableCopies": 10,
  "authorIds": [1, 2]
}
```

### 2. BookUpdatedEvent
Published when a book is updated.

**Routing Key:** `book.updated`

**Payload:**
```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "timestamp": "2024-11-29T10:30:00",
  "eventType": "BOOK_UPDATED",
  "bookId": 1,
  "isbn": "978-0-123456-78-9",
  "title": "Updated Book Title",
  "description": "Updated description",
  "publicationYear": 2024,
  "genre": "Fiction",
  "totalCopies": 10,
  "availableCopies": 8,
  "authorIds": [1, 2],
  "previousAvailableCopies": 10
}
```

### 3. BookDeletedEvent
Published when a book is deleted from the catalog.

**Routing Key:** `book.deleted`

**Payload:**
```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "timestamp": "2024-11-29T10:30:00",
  "eventType": "BOOK_DELETED",
  "bookId": 1,
  "isbn": "978-0-123456-78-9",
  "title": "Deleted Book Title"
}
```

### 4. BookAvailabilityChangedEvent
Published when book availability changes (e.g., when copies are borrowed or returned).

**Routing Key:** `book.availability.changed`

**Payload:**
```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "timestamp": "2024-11-29T10:30:00",
  "eventType": "BOOK_AVAILABILITY_CHANGED",
  "bookId": 1,
  "isbn": "978-0-123456-78-9",
  "title": "Book Title",
  "previousAvailableCopies": 10,
  "currentAvailableCopies": 8,
  "totalCopies": 10,
  "changeReason": "Book updated"
}
```

## RabbitMQ Configuration

### Exchange
- **Name:** `bookstore.events`
- **Type:** Topic
- **Durable:** Yes

### Queues
All queues are durable with the following configuration:
- **TTL:** 24 hours (86400000 ms)
- **Max Length:** 10,000 messages
- **Dead Letter Exchange:** `bookstore.dlx`

1. `book.created` - Bound to routing key `book.created`
2. `book.updated` - Bound to routing key `book.updated`
3. `book.deleted` - Bound to routing key `book.deleted`
4. `book.availability.changed` - Bound to routing key `book.availability.*`

## Correlation IDs

All events include a correlation ID for distributed tracing:
- If a correlation ID exists in the MDC (Mapped Diagnostic Context), it will be used
- Otherwise, a new UUID will be generated
- The correlation ID is propagated through the message headers

## Event Publishing Flow

1. **Book Operation** - A book is created, updated, or deleted
2. **Event Creation** - An event object is created with all relevant data
3. **Correlation ID** - A correlation ID is added from MDC or generated
4. **JSON Serialization** - The event is serialized to JSON
5. **RabbitMQ Publishing** - The event is published to the exchange with the appropriate routing key
6. **Message Properties** - Correlation ID and content type are set in message headers

## Error Handling

Event publishing failures are logged but do not fail the main operation:
- If event serialization fails, an error is logged
- If RabbitMQ publishing fails, an error is logged
- The book operation (create/update/delete) completes successfully regardless

This ensures that temporary messaging infrastructure issues don't impact core business operations.

## Configuration

### application.yml
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: bookstore
    password: bookstore123
    virtual-host: /bookstore
    connection-timeout: 30000
    requested-heartbeat: 30
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      retry:
        enabled: true
        initial-interval: 1000
        max-attempts: 3
        multiplier: 2.0
        max-interval: 10000
```

## Testing

### Unit Tests
- `BookEventPublisherTest` - Tests event publishing with mocked RabbitTemplate
- Verifies correct routing keys, exchange names, and message content

### Integration Tests
- `BookServiceEventIntegrationTest` - Tests event publishing from BookService
- Verifies events are published for all book operations
- Verifies availability change events are published when appropriate

## Usage by Other Services

Other microservices can consume these events by:

1. Creating a listener for the appropriate queue
2. Deserializing the JSON payload to the event class
3. Processing the event based on business logic

Example consumer:
```java
@RabbitListener(queues = "book.created")
public void handleBookCreated(String eventJson) {
    BookCreatedEvent event = objectMapper.readValue(eventJson, BookCreatedEvent.class);
    // Process the event
}
```

## Monitoring

Monitor event publishing through:
- RabbitMQ Management UI (http://localhost:15672)
- Application logs (correlation IDs for tracing)
- Prometheus metrics (if configured)
- Dead letter queue for failed messages

## Future Enhancements

Potential improvements:
1. Add event versioning for backward compatibility
2. Implement event replay capability
3. Add event schema validation
4. Implement event sourcing for audit trail
5. Add metrics for event publishing success/failure rates
