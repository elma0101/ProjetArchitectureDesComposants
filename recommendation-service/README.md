# Recommendation Service

The Recommendation Service is a microservice responsible for generating personalized book recommendations, tracking user preferences, and providing analytics on book popularity and borrowing patterns.

## Features

- **Personalized Recommendations**: Generate book recommendations based on user preferences and borrowing history
- **Multiple Recommendation Types**:
  - Popular books
  - Trending books
  - Content-based recommendations (based on favorite authors)
  - Collaborative filtering (future enhancement)
- **User Preference Tracking**: Track user borrowing history, favorite authors, and book ratings
- **Analytics**: Provide insights on book popularity, borrowing patterns, and ratings
- **Event-Driven Updates**: Listen to loan events to automatically update analytics and preferences

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Database**: MongoDB (NoSQL for flexible schema)
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Messaging**: RabbitMQ
- **API Communication**: OpenFeign
- **Resilience**: Resilience4j Circuit Breaker
- **Monitoring**: Spring Boot Actuator, Prometheus

## API Endpoints

### Recommendations

- `GET /api/recommendations/user/{userId}` - Get recommendations for a user
- `GET /api/recommendations/user/{userId}/type/{type}` - Get recommendations by type
- `POST /api/recommendations/user/{userId}/refresh` - Refresh recommendations

### User Preferences

- `GET /api/preferences/user/{userId}` - Get user preferences
- `POST /api/preferences/feedback` - Record user feedback/rating
- `POST /api/preferences/user/{userId}/favorite-author/{authorId}` - Add favorite author
- `POST /api/preferences/user/{userId}/favorite-category?category={category}` - Add favorite category

### Analytics

- `GET /api/analytics/popular-books?limit={limit}` - Get popular books
- `GET /api/analytics/most-borrowed?limit={limit}` - Get most borrowed books
- `GET /api/analytics/top-rated?limit={limit}` - Get top rated books
- `GET /api/analytics/book/{bookId}` - Get analytics for a specific book

### Health

- `GET /api/health` - Service health check
- `GET /actuator/health` - Detailed health information

## Configuration

### Environment Variables

- `MONGODB_HOST`: MongoDB host (default: localhost)
- `MONGODB_PORT`: MongoDB port (default: 27017)
- `MONGODB_DATABASE`: MongoDB database name (default: recommendation_db)
- `MONGODB_USERNAME`: MongoDB username
- `MONGODB_PASSWORD`: MongoDB password
- `RABBITMQ_HOST`: RabbitMQ host (default: localhost)
- `RABBITMQ_PORT`: RabbitMQ port (default: 5672)
- `RABBITMQ_USERNAME`: RabbitMQ username (default: guest)
- `RABBITMQ_PASSWORD`: RabbitMQ password (default: guest)
- `EUREKA_SERVER_URL`: Eureka server URL (default: http://localhost:8761/eureka/)
- `CONFIG_SERVER_URL`: Config server URL (default: http://localhost:8888)
- `SERVER_PORT`: Service port (default: 8084)

## Running the Service

### Local Development

```bash
# Start MongoDB
docker run -d -p 27017:27017 --name mongodb mongo:7.0

# Start RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:3.12-management

# Run the service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
# Build the service
./build.sh

# Run with Docker Compose
docker-compose up -d
```

### Kubernetes

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml
```

## Recommendation Algorithms

### Popular Recommendations
Based on popularity score calculated from:
- Total borrows (70% weight)
- Average rating (30% weight)

### Trending Recommendations
Based on recent borrowing activity and total borrows.

### Content-Based Recommendations
Based on user preferences:
- Favorite authors
- Favorite categories
- Previous borrowing history

### Future Enhancements
- Collaborative filtering using user similarity
- Machine learning models for better predictions
- Real-time recommendation updates

## Event Handling

The service listens to loan events from the Loan Management Service:

- **BORROWED**: Updates book analytics and user preferences
- **RETURNED**: Updates active borrow count

## MongoDB Collections

### recommendations
Stores generated recommendations for users.

### user_preferences
Stores user preferences, favorite authors, categories, and ratings.

### book_analytics
Stores analytics data for books including borrows, ratings, and popularity scores.

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Monitoring

- Prometheus metrics: `http://localhost:8084/actuator/prometheus`
- Health check: `http://localhost:8084/actuator/health`
- Service info: `http://localhost:8084/actuator/info`

## Dependencies

- Book Catalog Service (for book information)
- Loan Management Service (for loan events)
- RabbitMQ (for event messaging)
- MongoDB (for data storage)
- Eureka Server (for service discovery)
- Config Server (for configuration management)
