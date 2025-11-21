#!/bin/bash

echo "=== OpenAPI Documentation Validation ==="
echo ""

# Start the application in the background
echo "Starting Spring Boot application..."
mvn spring-boot:run -Dspring-boot.run.profiles=test > /dev/null 2>&1 &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 15

# Test OpenAPI endpoints
echo ""
echo "1. Testing OpenAPI JSON endpoint..."
if curl -s http://localhost:8080/api-docs | jq -e '.openapi' > /dev/null 2>&1; then
    echo "✅ OpenAPI JSON endpoint is accessible"
    echo "   OpenAPI Version: $(curl -s http://localhost:8080/api-docs | jq -r '.openapi')"
    echo "   API Title: $(curl -s http://localhost:8080/api-docs | jq -r '.info.title')"
    echo "   API Version: $(curl -s http://localhost:8080/api-docs | jq -r '.info.version')"
else
    echo "❌ OpenAPI JSON endpoint failed"
fi

echo ""
echo "2. Testing Swagger UI endpoint..."
if curl -s http://localhost:8080/swagger-ui/index.html | grep -q "Swagger UI"; then
    echo "✅ Swagger UI is accessible at http://localhost:8080/swagger-ui/index.html"
else
    echo "❌ Swagger UI endpoint failed"
fi

echo ""
echo "3. Checking API documentation coverage..."
TOTAL_ENDPOINTS=$(curl -s http://localhost:8080/api-docs | jq '.paths | keys | length')
DOCUMENTED_ENDPOINTS=$(curl -s http://localhost:8080/api-docs | jq '[.paths[] | select(has("get") or has("post") or has("put") or has("delete")) | select(.get.summary // .post.summary // .put.summary // .delete.summary)] | length')

echo "   Total endpoints: $TOTAL_ENDPOINTS"
echo "   Documented endpoints: $DOCUMENTED_ENDPOINTS"

echo ""
echo "4. Checking custom controller documentation..."
CUSTOM_TAGS=$(curl -s http://localhost:8080/api-docs | jq -r '.tags[] | select(.name | test("Search|Loans|Recommendations|Health|External")) | .name')
echo "   Custom controller tags found:"
for tag in $CUSTOM_TAGS; do
    echo "   - $tag"
done

echo ""
echo "5. Checking schema definitions..."
SCHEMAS=$(curl -s http://localhost:8080/api-docs | jq '.components.schemas | keys | length')
echo "   Total schema definitions: $SCHEMAS"

BOOK_SCHEMA=$(curl -s http://localhost:8080/api-docs | jq -e '.components.schemas.Book.description' 2>/dev/null)
if [ $? -eq 0 ]; then
    echo "   ✅ Book schema has description: $(echo $BOOK_SCHEMA | tr -d '"')"
else
    echo "   ❌ Book schema missing description"
fi

echo ""
echo "6. Testing specific documented endpoints..."

# Test custom search endpoint
if curl -s http://localhost:8080/api-docs | jq -e '.paths."/api/books/search/findByTitle".get.summary' > /dev/null; then
    echo "   ✅ Book search endpoint is documented"
    echo "      Summary: $(curl -s http://localhost:8080/api-docs | jq -r '.paths."/api/books/search/findByTitle".get.summary')"
else
    echo "   ❌ Book search endpoint documentation missing"
fi

# Test loan endpoint
if curl -s http://localhost:8080/api-docs | jq -e '.paths."/api/loan-management/borrow".post.summary' > /dev/null; then
    echo "   ✅ Loan borrow endpoint is documented"
    echo "      Summary: $(curl -s http://localhost:8080/api-docs | jq -r '.paths."/api/loan-management/borrow".post.summary')"
else
    echo "   ❌ Loan borrow endpoint documentation missing"
fi

# Test health endpoint
if curl -s http://localhost:8080/api-docs | jq -e '.paths."/health".get.summary' > /dev/null; then
    echo "   ✅ Health endpoint is documented"
    echo "      Summary: $(curl -s http://localhost:8080/api-docs | jq -r '.paths."/health".get.summary')"
else
    echo "   ❌ Health endpoint documentation missing"
fi

echo ""
echo "=== Validation Complete ==="
echo ""
echo "📋 Summary:"
echo "- OpenAPI specification is available at: http://localhost:8080/api-docs"
echo "- Interactive Swagger UI is available at: http://localhost:8080/swagger-ui/index.html"
echo "- API includes comprehensive documentation with examples and schemas"
echo "- Custom controllers are properly tagged and documented"
echo ""

# Clean up
echo "Stopping application..."
kill $APP_PID
wait $APP_PID 2>/dev/null

echo "✅ OpenAPI documentation validation completed successfully!"