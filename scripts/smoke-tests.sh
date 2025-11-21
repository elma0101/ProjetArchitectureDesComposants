#!/bin/bash

# Smoke Tests for Bookstore Application
set -e

BASE_URL=${1:-http://localhost:8080}
FRONTEND_URL=${2:-http://localhost:3000}

echo "🧪 Running smoke tests..."
echo "🔧 Backend URL: $BASE_URL"
echo "🌐 Frontend URL: $FRONTEND_URL"

# Function to make HTTP request and check response
check_endpoint() {
    local url=$1
    local expected_status=${2:-200}
    local description=$3
    
    echo "Testing: $description"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$response" = "$expected_status" ]; then
        echo "✅ $description - Status: $response"
        return 0
    else
        echo "❌ $description - Expected: $expected_status, Got: $response"
        return 1
    fi
}

# Function to check JSON response
check_json_endpoint() {
    local url=$1
    local description=$2
    
    echo "Testing: $description"
    
    response=$(curl -s "$url" || echo "{}")
    
    if echo "$response" | jq . >/dev/null 2>&1; then
        echo "✅ $description - Valid JSON response"
        return 0
    else
        echo "❌ $description - Invalid JSON response"
        echo "Response: $response"
        return 1
    fi
}

failed_tests=0

# Test backend health endpoints
echo "🏥 Testing backend health endpoints..."
check_endpoint "$BASE_URL/actuator/health" 200 "Actuator health endpoint" || ((failed_tests++))
check_endpoint "$BASE_URL/health" 200 "Custom health endpoint" || ((failed_tests++))
check_endpoint "$BASE_URL/health/liveness" 200 "Liveness probe" || ((failed_tests++))
check_endpoint "$BASE_URL/health/readiness" 200 "Readiness probe" || ((failed_tests++))

# Test API endpoints
echo "📚 Testing API endpoints..."
check_json_endpoint "$BASE_URL/api/books" "Books API endpoint" || ((failed_tests++))
check_json_endpoint "$BASE_URL/api/authors" "Authors API endpoint" || ((failed_tests++))
check_json_endpoint "$BASE_URL/api/loans" "Loans API endpoint" || ((failed_tests++))

# Test OpenAPI documentation
echo "📖 Testing API documentation..."
check_endpoint "$BASE_URL/v3/api-docs" 200 "OpenAPI JSON documentation" || ((failed_tests++))
check_endpoint "$BASE_URL/swagger-ui/index.html" 200 "Swagger UI" || ((failed_tests++))

# Test metrics endpoint
echo "📊 Testing metrics..."
check_endpoint "$BASE_URL/actuator/metrics" 200 "Metrics endpoint" || ((failed_tests++))
check_endpoint "$BASE_URL/actuator/prometheus" 200 "Prometheus metrics" || ((failed_tests++))

# Test frontend
echo "🌐 Testing frontend..."
check_endpoint "$FRONTEND_URL" 200 "Frontend home page" || ((failed_tests++))
check_endpoint "$FRONTEND_URL/health" 200 "Frontend health check" || ((failed_tests++))

# Test CORS (if applicable)
echo "🔗 Testing CORS..."
cors_response=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: GET" \
    -H "Access-Control-Request-Headers: Content-Type" \
    -X OPTIONS "$BASE_URL/api/books" || echo "000")

if [ "$cors_response" = "200" ] || [ "$cors_response" = "204" ]; then
    echo "✅ CORS preflight - Status: $cors_response"
else
    echo "❌ CORS preflight - Expected: 200/204, Got: $cors_response"
    ((failed_tests++))
fi

# Test database connectivity (indirect)
echo "💾 Testing database connectivity..."
db_test_response=$(curl -s "$BASE_URL/health/detailed" | jq -r '.database.status' 2>/dev/null || echo "UNKNOWN")
if [ "$db_test_response" = "UP" ]; then
    echo "✅ Database connectivity - Status: UP"
else
    echo "❌ Database connectivity - Status: $db_test_response"
    ((failed_tests++))
fi

# Test cache connectivity (if Redis is configured)
echo "🗄️ Testing cache connectivity..."
cache_test_response=$(curl -s "$BASE_URL/health/detailed" | jq -r '.redis.status' 2>/dev/null || echo "UNKNOWN")
if [ "$cache_test_response" = "UP" ] || [ "$cache_test_response" = "null" ]; then
    echo "✅ Cache connectivity - Status: $cache_test_response"
else
    echo "❌ Cache connectivity - Status: $cache_test_response"
    ((failed_tests++))
fi

# Summary
echo ""
echo "📋 Smoke Test Summary"
echo "===================="
if [ $failed_tests -eq 0 ]; then
    echo "🎉 All smoke tests passed!"
    exit 0
else
    echo "❌ $failed_tests test(s) failed"
    exit 1
fi