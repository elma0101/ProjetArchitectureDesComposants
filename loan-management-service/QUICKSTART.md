# Loan Management Service - Quick Start Guide

## Prerequisites

Before starting the Loan Management Service, ensure the following services are running:

1. **PostgreSQL Database** (port 5432 or 5435)
2. **Eureka Server** (port 8761)
3. **Config Server** (port 8888) - Optional but recommended
4. **Book Catalog Service** (port 8082)
5. **User Management Service** (port 8081)

## Quick Start Options

### Option 1: Run with Maven (Development)

```bash
# Create the database
createdb loan_management_db

# Run the service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on **http://localhost:8083**

### Option 2: Run with Docker Compose (Recommended)

```bash
# Build the service
./build.sh

# Start the service and database
docker-compose up -d

# Check logs
docker-compose logs -f loan-management-service

# Stop the service
docker-compose down
```

### Option 3: Run with Kubernetes

```bash
# Apply the deployment
kubectl apply -f k8s/deployment.yaml

# Check the pods
kubectl get pods -n bookstore

# Check the service
kubectl get svc -n bookstore

# View logs
kubectl logs -f deployment/loan-management-service -n bookstore
```

## Verify the Service is Running

### Health Check

```bash
# Simple health check
curl http://localhost:8083/api/health

# Detailed health check
curl http://localhost:8083/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "loan-management-service"
}
```

### Check Eureka Registration

Visit: http://localhost:8761

You should see `LOAN-MANAGEMENT-SERVICE` registered.

### Check Metrics

```bash
# Prometheus metrics
curl http://localhost:8083/actuator/metrics

# Specific metric
curl http://localhost:8083/actuator/metrics/jvm.memory.used
```

## Database Setup

### Manual Database Creation

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE loan_management_db;

-- Create user
CREATE USER bookstore WITH PASSWORD 'bookstore123';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE loan_management_db TO bookstore;

-- Connect to the database
\c loan_management_db

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO bookstore;
```

### Verify Flyway Migrations

```bash
# Check if tables were created
psql -U bookstore -d loan_management_db -c "\dt"
```

You should see:
- `loans`
- `loan_tracking`
- `flyway_schema_history`

## Testing the Service

### Test Feign Clients

The service will attempt to connect to:
- Book Catalog Service at `http://book-catalog-service:8082` (or via Eureka)
- User Management Service at `http://user-management-service:8081` (or via Eureka)

If these services are not available, the circuit breaker will activate and fallback methods will be called.

### Check Circuit Breaker Status

```bash
curl http://localhost:8083/actuator/health | jq '.components.circuitBreakers'
```

## Common Issues and Solutions

### Issue: Service won't start

**Solution:**
1. Check if PostgreSQL is running: `pg_isready`
2. Verify database exists: `psql -l | grep loan_management_db`
3. Check port 8083 is not in use: `lsof -i :8083`

### Issue: Cannot connect to Eureka

**Solution:**
1. Verify Eureka is running: `curl http://localhost:8761`
2. Check network connectivity
3. Review logs: `docker-compose logs loan-management-service`

### Issue: Feign clients failing

**Solution:**
1. Ensure Book Catalog Service is registered with Eureka
2. Ensure User Management Service is registered with Eureka
3. Check circuit breaker status in actuator health endpoint
4. Review Feign client logs (set `feign: DEBUG` in logging config)

### Issue: Database migration fails

**Solution:**
1. Check database user permissions
2. Verify Flyway configuration in `application.yml`
3. Check migration scripts in `src/main/resources/db/migration`
4. Review Flyway history: `SELECT * FROM flyway_schema_history;`

## Environment Variables

Override default configuration with environment variables:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/loan_management_db
export SPRING_DATASOURCE_USERNAME=bookstore
export SPRING_DATASOURCE_PASSWORD=bookstore123

# Eureka
export EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/

# Config Server
export SPRING_CLOUD_CONFIG_URI=http://localhost:8888

# Run the service
java -jar target/loan-management-service-1.0.0.jar
```

## Development Tips

### Enable Debug Logging

Add to `application-dev.yml`:
```yaml
logging:
  level:
    com.bookstore.loanmanagement: DEBUG
    org.springframework.cloud: DEBUG
    feign: DEBUG
```

### Hot Reload with Spring DevTools

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Test with H2 Database

Use the test profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## Next Steps

Once the service is running:

1. Implement loan business logic (Task 14)
2. Create loan management APIs (Task 15)
3. Implement distributed transaction handling (Task 16)

## Useful Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run tests only
mvn test

# Check dependencies
mvn dependency:tree

# Generate project report
mvn site

# Docker build
docker build -t bookstore/loan-management-service:latest .

# Docker run
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/loan_management_db \
  bookstore/loan-management-service:latest
```

## Support

For issues or questions:
1. Check the logs: `docker-compose logs -f loan-management-service`
2. Review the README.md for detailed documentation
3. Check the IMPLEMENTATION_SUMMARY.md for technical details

---

**Service Port**: 8083
**Database Port**: 5432 (5435 in Docker Compose)
**Health Check**: http://localhost:8083/actuator/health
**Metrics**: http://localhost:8083/actuator/prometheus
