# Bookstore Application Deployment Guide

This guide covers the deployment and production readiness setup for the Bookstore application.

## 🏗️ Architecture Overview

The application consists of:
- **Backend**: Spring Boot REST API (Java 17)
- **Frontend**: React application (Node.js 18)
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Reverse Proxy**: Nginx (for frontend)

## 🚀 Quick Start with Docker Compose

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- 4GB+ RAM available

### 1. Clone and Setup
```bash
git clone <repository-url>
cd bookstore-app
cp .env.example .env
# Edit .env with your configuration
```

### 2. Deploy
```bash
./scripts/deploy.sh production latest
```

### 3. Verify Deployment
```bash
./scripts/smoke-tests.sh
```

## 🐳 Docker Configuration

### Multi-stage Builds
Both backend and frontend use multi-stage Docker builds for optimization:

- **Backend**: Maven build stage + JRE runtime stage
- **Frontend**: Node.js build stage + Nginx runtime stage

### Security Features
- Non-root user execution
- Minimal base images (Alpine Linux)
- Health checks included
- Resource limits configured

## ⚙️ Configuration Management

### Environment Variables
Key configuration through environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://postgres:5432/bookstore_prod
DATABASE_USERNAME=bookstore_user
DATABASE_PASSWORD=your_secure_password

# Security
JWT_SECRET=your_jwt_secret_minimum_256_bits

# External Services
EXTERNAL_BOOK_SERVICE_URL=https://api.books.example.com
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# Feature Toggles
FEATURE_RECOMMENDATIONS=true
FEATURE_CACHING=true
```

### Configuration Validation
Production configuration is validated at startup:
- Required properties presence
- JWT secret minimum length
- Database connectivity

## 🏥 Health Checks and Monitoring

### Health Endpoints
- `/actuator/health` - Spring Boot Actuator health
- `/health` - Custom health endpoint
- `/health/liveness` - Kubernetes liveness probe
- `/health/readiness` - Kubernetes readiness probe
- `/health/detailed` - Detailed health with dependencies

### Monitoring Stack
- **Metrics**: Micrometer + Prometheus
- **Logging**: Structured JSON logging
- **Tracing**: Spring Cloud Sleuth ready
- **Dashboards**: Grafana compatible

## 🔒 Security Configuration

### Production Security Features
- HTTPS enforcement
- CORS configuration
- Rate limiting
- JWT token authentication
- Input validation
- SQL injection prevention
- XSS protection

### Security Headers
```
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
```

## 🎯 Performance Optimization

### Backend Optimizations
- Connection pooling (HikariCP)
- Redis caching
- JVM tuning for containers
- Database query optimization
- Pagination for large datasets

### Frontend Optimizations
- Gzip compression
- Static asset caching
- Code splitting ready
- CDN ready

## ☸️ Kubernetes Deployment

### Prerequisites
- Kubernetes 1.20+
- kubectl configured
- Ingress controller (nginx)
- Cert-manager (for TLS)

### Deploy to Kubernetes
```bash
# Deploy to default namespace
./scripts/k8s-deploy.sh

# Deploy to custom namespace
./scripts/k8s-deploy.sh my-namespace production
```

### Kubernetes Features
- **High Availability**: 3 backend replicas, 2 frontend replicas
- **Auto-scaling**: HPA ready
- **Rolling Updates**: Zero-downtime deployments
- **Health Probes**: Liveness, readiness, and startup probes
- **Resource Limits**: CPU and memory limits configured
- **Persistent Storage**: Logs and uploads persistence
- **TLS Termination**: Automatic HTTPS with Let's Encrypt

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow
The CI/CD pipeline includes:

1. **Testing Phase**
   - Unit tests (backend & frontend)
   - Integration tests
   - Security scanning

2. **Build Phase**
   - Multi-arch Docker builds
   - Container registry push
   - Vulnerability scanning

3. **Deploy Phase**
   - Staging deployment (develop branch)
   - Production deployment (main branch)
   - Smoke tests
   - Rollback on failure

### Pipeline Triggers
- **Pull Requests**: Run tests and security scans
- **Develop Branch**: Deploy to staging
- **Main Branch**: Deploy to production

## 📊 Monitoring and Observability

### Metrics Collection
- Application metrics via Micrometer
- JVM metrics
- Database connection pool metrics
- Custom business metrics
- HTTP request metrics

### Log Management
- Structured JSON logging
- Centralized log collection ready
- Log rotation and retention
- Correlation IDs for tracing

### Alerting
- Health check failures
- High error rates
- Resource utilization
- Database connectivity issues

## 🔧 Operational Commands

### Docker Compose Operations
```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Scale services
docker-compose up -d --scale backend=3

# Update services
docker-compose pull && docker-compose up -d

# Backup database
docker-compose exec postgres pg_dump -U bookstore_user bookstore_prod > backup.sql
```

### Kubernetes Operations
```bash
# View application status
kubectl get all -n bookstore

# Scale backend
kubectl scale deployment bookstore-backend --replicas=5 -n bookstore

# View logs
kubectl logs -f deployment/bookstore-backend -n bookstore

# Port forward for debugging
kubectl port-forward service/bookstore-backend-service 8080:8080 -n bookstore

# Rolling update
kubectl set image deployment/bookstore-backend backend=new-image:tag -n bookstore
```

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check database connectivity
   curl http://localhost:8080/health/detailed
   
   # Check database logs
   docker-compose logs postgres
   ```

2. **Memory Issues**
   ```bash
   # Check container memory usage
   docker stats
   
   # Adjust JVM memory settings
   export JAVA_OPTS="-Xmx1g -Xms512m"
   ```

3. **Performance Issues**
   ```bash
   # Check metrics
   curl http://localhost:8080/actuator/metrics
   
   # Enable debug logging
   export LOG_LEVEL=DEBUG
   ```

### Health Check Failures
- Verify database connectivity
- Check Redis availability
- Validate configuration
- Review application logs

## 📋 Deployment Checklist

### Pre-deployment
- [ ] Environment variables configured
- [ ] Database migrations ready
- [ ] SSL certificates configured
- [ ] Monitoring setup verified
- [ ] Backup procedures tested

### Post-deployment
- [ ] Health checks passing
- [ ] Smoke tests successful
- [ ] Monitoring alerts configured
- [ ] Performance baseline established
- [ ] Documentation updated

## 🔄 Rollback Procedures

### Docker Compose Rollback
```bash
# Rollback to previous version
docker-compose down
docker-compose pull previous-tag
docker-compose up -d
```

### Kubernetes Rollback
```bash
# Rollback deployment
kubectl rollout undo deployment/bookstore-backend -n bookstore

# Check rollout status
kubectl rollout status deployment/bookstore-backend -n bookstore
```

## 📞 Support and Maintenance

### Regular Maintenance Tasks
- Database backup verification
- Log rotation and cleanup
- Security updates
- Performance monitoring review
- Capacity planning

### Emergency Contacts
- DevOps Team: devops@company.com
- Database Admin: dba@company.com
- Security Team: security@company.com

---

For more detailed information, refer to the individual configuration files and scripts in the repository.