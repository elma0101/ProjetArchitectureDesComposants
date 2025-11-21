# Multi-stage Docker build for Spring Boot application
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -g 1001 -S bookstore && \
    adduser -S bookstore -u 1001 -G bookstore

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /var/log/bookstore /var/bookstore/uploads && \
    chown -R bookstore:bookstore /var/log/bookstore /var/bookstore/uploads /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the JAR file
RUN chown bookstore:bookstore app.jar

# Switch to non-root user
USER bookstore

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]