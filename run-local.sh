#!/bin/bash

echo "🚀 Local Development Setup (No Docker)"
echo "======================================"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists java; then
    echo "❌ Java is not installed (required: Java 17+)"
    exit 1
fi

if ! command_exists mvn; then
    echo "❌ Maven is not installed"
    exit 1
fi

if ! command_exists npm; then
    echo "❌ Node.js/npm is not installed"
    exit 1
fi

echo "✅ All prerequisites are installed"

# Setup backend with H2 database
echo "🔧 Setting up backend with H2 database..."
cd backend

# Build backend
echo "🏗️  Building backend..."
mvn clean package -DskipTests -Dspring.profiles.active=h2

if [ $? -ne 0 ]; then
    echo "❌ Backend build failed"
    exit 1
fi

# Start backend in background
echo "🚀 Starting backend..."
nohup java -jar target/*.jar --spring.profiles.active=h2 > backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

cd ..

# Setup frontend
echo "🎨 Setting up frontend..."
cd frontend

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing frontend dependencies..."
    npm install
fi

# Start frontend in background
echo "🚀 Starting frontend..."
nohup npm start > frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

cd ..

# Save PIDs for cleanup
echo "$BACKEND_PID" > backend.pid
echo "$FRONTEND_PID" > frontend.pid

echo "✅ Setup complete!"
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:8080"
echo "📊 Backend Health: http://localhost:8080/actuator/health"
echo ""
echo "📝 Logs:"
echo "   Backend: backend/backend.log"
echo "   Frontend: frontend/frontend.log"
echo ""
echo "🛑 To stop services, run: ./stop-local.sh"