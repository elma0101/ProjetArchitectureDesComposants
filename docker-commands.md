# Docker Troubleshooting Commands

## Basic Troubleshooting

### Check Docker Status
```bash
# Check if Docker is running
docker info

# Check Docker version
docker --version
docker-compose --version
```

### View Container Status
```bash
# List all containers
docker-compose ps

# View detailed container info
docker-compose ps -a

# Check container logs
docker-compose logs [service-name]
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

### Clean Up Commands
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Remove orphaned containers
docker-compose down --remove-orphans

# Clean up Docker system
docker system prune -f

# Remove all unused images
docker image prune -a
```

### Build Commands
```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build backend
docker-compose build frontend

# Build without cache
docker-compose build --no-cache

# Force rebuild
docker-compose up --build
```

### Debug Commands
```bash
# Run container interactively
docker-compose run backend bash
docker-compose run frontend sh

# Execute command in running container
docker-compose exec backend bash
docker-compose exec postgres psql -U bookstore_user -d bookstore_prod

# View container resource usage
docker stats

# Inspect container configuration
docker-compose config
```

## Specific Error Solutions

### Frontend Build Errors
```bash
# If npm install fails
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install

# If React build fails
npm run build
```

### Backend Build Errors
```bash
# If Maven build fails
cd backend
mvn clean package -DskipTests

# Use H2 profile for development
mvn clean package -DskipTests -Dspring.profiles.active=h2
```

### Database Connection Errors
```bash
# Check if PostgreSQL is running
docker-compose logs postgres

# Connect to PostgreSQL
docker-compose exec postgres psql -U bookstore_user -d bookstore_prod

# Check Redis
docker-compose exec redis redis-cli ping
```

### Port Conflicts
```bash
# Check what's using port 3000
lsof -i :3000

# Check what's using port 8080
lsof -i :8080

# Kill process using port
kill -9 $(lsof -t -i:3000)
```

### Permission Issues
```bash
# Fix Docker permissions (Linux)
sudo usermod -aG docker $USER
newgrp docker

# Fix file permissions
sudo chown -R $USER:$USER .
```

## Environment Variables
```bash
# Check environment variables
docker-compose config

# Override environment variables
POSTGRES_PASSWORD=newpass docker-compose up
```

## Health Checks
```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Check frontend health
curl http://localhost:3000/health

# Check if services are responding
curl -f http://localhost:3000 || echo "Frontend not responding"
curl -f http://localhost:8080/actuator/health || echo "Backend not responding"
```