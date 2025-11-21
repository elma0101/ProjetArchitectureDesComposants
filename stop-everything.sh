#!/bin/bash

echo "🛑 Stopping ALL Bookstore Application Services"
echo "=============================================="

# Stop local development services
echo "📱 Stopping local development services..."
if [ -f "./stop-local.sh" ]; then
    ./stop-local.sh
else
    echo "⚠️  stop-local.sh not found, continuing..."
fi

# Stop Docker services
echo "🐳 Stopping Docker services..."
docker-compose down -v --remove-orphans 2>/dev/null || echo "⚠️  Docker compose not running"

# Kill any remaining Java processes (backend)
echo "☕ Killing Java/Backend processes..."
pkill -f "java.*bookstore" 2>/dev/null || echo "   No Java bookstore processes found"
pkill -f "BookstoreApplication" 2>/dev/null || echo "   No BookstoreApplication processes found"

# Kill any remaining Node.js processes (frontend)
echo "🎨 Killing Node.js/Frontend processes..."
pkill -f "npm.*start" 2>/dev/null || echo "   No npm start processes found"
pkill -f "react-scripts" 2>/dev/null || echo "   No react-scripts processes found"
pkill -f "node.*3000" 2>/dev/null || echo "   No Node.js processes on port 3000 found"

# Kill processes using specific ports
echo "🔌 Freeing up ports 3000 and 8080..."
for port in 3000 8080; do
    PID=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$PID" ]; then
        echo "   Killing process $PID using port $port"
        kill -9 $PID 2>/dev/null || echo "   Failed to kill process $PID"
    else
        echo "   Port $port is free"
    fi
done

# Clean up any remaining PID files
echo "🧹 Cleaning up PID files..."
rm -f backend.pid frontend.pid 2>/dev/null

# Clean up log files (optional)
echo "📝 Cleaning up log files..."
rm -f backend/backend.log frontend/frontend.log 2>/dev/null

# Final verification
echo "🔍 Final verification..."
RUNNING_PROCESSES=$(ps aux | grep -E "(bookstore|java.*8080|npm.*3000|react-scripts)" | grep -v grep | wc -l)
USED_PORTS=$(ss -tlnp | grep -E ":3000|:8080" | wc -l)

if [ $RUNNING_PROCESSES -eq 0 ] && [ $USED_PORTS -eq 0 ]; then
    echo "✅ All bookstore services stopped successfully!"
    echo "   - No related processes running"
    echo "   - Ports 3000 and 8080 are free"
else
    echo "⚠️  Some services might still be running:"
    if [ $RUNNING_PROCESSES -gt 0 ]; then
        echo "   - $RUNNING_PROCESSES related processes still running"
        ps aux | grep -E "(bookstore|java.*8080|npm.*3000|react-scripts)" | grep -v grep
    fi
    if [ $USED_PORTS -gt 0 ]; then
        echo "   - Ports still in use:"
        ss -tlnp | grep -E ":3000|:8080"
    fi
fi

echo ""
echo "🚀 To restart the application:"
echo "   Local development: ./run-local.sh"
echo "   Docker: docker-compose up -d"
echo "   Troubleshooting: ./docker-troubleshoot.sh"