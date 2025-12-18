#!/bin/bash

# RabbitMQ Health Check Script
# This script verifies RabbitMQ is running and properly configured

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
RABBITMQ_HOST="${RABBITMQ_HOST:-localhost}"
RABBITMQ_PORT="${RABBITMQ_PORT:-15672}"
RABBITMQ_USER="${RABBITMQ_USER:-bookstore}"
RABBITMQ_PASS="${RABBITMQ_PASS:-bookstore123}"
RABBITMQ_VHOST="${RABBITMQ_VHOST:-/bookstore}"

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${NC}ℹ${NC} $1"
}

# Check if RabbitMQ is accessible
check_rabbitmq_accessible() {
    print_info "Checking if RabbitMQ is accessible..."
    
    if curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/overview" > /dev/null 2>&1; then
        print_success "RabbitMQ is accessible at http://$RABBITMQ_HOST:$RABBITMQ_PORT"
        return 0
    else
        print_error "Cannot connect to RabbitMQ at http://$RABBITMQ_HOST:$RABBITMQ_PORT"
        return 1
    fi
}

# Check virtual host
check_vhost() {
    print_info "Checking virtual host '$RABBITMQ_VHOST'..."
    
    VHOST_ENCODED=$(echo "$RABBITMQ_VHOST" | sed 's/\//%2F/g')
    
    if curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/vhosts/$VHOST_ENCODED" > /dev/null 2>&1; then
        print_success "Virtual host '$RABBITMQ_VHOST' exists"
        return 0
    else
        print_error "Virtual host '$RABBITMQ_VHOST' not found"
        return 1
    fi
}

# Check exchanges
check_exchanges() {
    print_info "Checking exchanges..."
    
    VHOST_ENCODED=$(echo "$RABBITMQ_VHOST" | sed 's/\//%2F/g')
    EXCHANGES=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/exchanges/$VHOST_ENCODED")
    
    EXPECTED_EXCHANGES=("bookstore.events" "bookstore.dlx")
    
    for exchange in "${EXPECTED_EXCHANGES[@]}"; do
        if echo "$EXCHANGES" | grep -q "\"name\":\"$exchange\""; then
            print_success "Exchange '$exchange' exists"
        else
            print_error "Exchange '$exchange' not found"
        fi
    done
}

# Check queues
check_queues() {
    print_info "Checking queues..."
    
    VHOST_ENCODED=$(echo "$RABBITMQ_VHOST" | sed 's/\//%2F/g')
    QUEUES=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/queues/$VHOST_ENCODED")
    
    EXPECTED_QUEUES=(
        "book.created"
        "book.updated"
        "book.deleted"
        "book.availability.changed"
        "loan.created"
        "loan.returned"
        "loan.overdue"
        "notification.email"
        "audit.log"
        "recommendation.update"
        "dead-letter.queue"
    )
    
    for queue in "${EXPECTED_QUEUES[@]}"; do
        if echo "$QUEUES" | grep -q "\"name\":\"$queue\""; then
            # Get queue details
            QUEUE_INFO=$(echo "$QUEUES" | grep -A 10 "\"name\":\"$queue\"")
            MESSAGES=$(echo "$QUEUE_INFO" | grep -o '"messages":[0-9]*' | head -1 | cut -d':' -f2)
            CONSUMERS=$(echo "$QUEUE_INFO" | grep -o '"consumers":[0-9]*' | head -1 | cut -d':' -f2)
            
            print_success "Queue '$queue' exists (messages: ${MESSAGES:-0}, consumers: ${CONSUMERS:-0})"
        else
            print_error "Queue '$queue' not found"
        fi
    done
}

# Check bindings
check_bindings() {
    print_info "Checking bindings..."
    
    VHOST_ENCODED=$(echo "$RABBITMQ_VHOST" | sed 's/\//%2F/g')
    BINDINGS=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/bindings/$VHOST_ENCODED")
    
    BINDING_COUNT=$(echo "$BINDINGS" | grep -o '"source":"bookstore.events"' | wc -l)
    
    if [ "$BINDING_COUNT" -gt 0 ]; then
        print_success "Found $BINDING_COUNT bindings for bookstore.events exchange"
    else
        print_warning "No bindings found for bookstore.events exchange"
    fi
}

# Check connections
check_connections() {
    print_info "Checking connections..."
    
    CONNECTIONS=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/connections")
    CONNECTION_COUNT=$(echo "$CONNECTIONS" | grep -o '"name"' | wc -l)
    
    print_info "Active connections: $CONNECTION_COUNT"
}

# Check node health
check_node_health() {
    print_info "Checking node health..."
    
    NODE_HEALTH=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/healthchecks/node")
    
    if echo "$NODE_HEALTH" | grep -q '"status":"ok"'; then
        print_success "Node health check passed"
        
        # Get memory and disk info
        OVERVIEW=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/overview")
        
        MEM_USED=$(echo "$OVERVIEW" | grep -o '"mem_used":[0-9]*' | cut -d':' -f2)
        MEM_LIMIT=$(echo "$OVERVIEW" | grep -o '"mem_limit":[0-9]*' | cut -d':' -f2)
        
        if [ -n "$MEM_USED" ] && [ -n "$MEM_LIMIT" ]; then
            MEM_PERCENT=$((MEM_USED * 100 / MEM_LIMIT))
            print_info "Memory usage: $MEM_PERCENT%"
            
            if [ "$MEM_PERCENT" -gt 80 ]; then
                print_warning "Memory usage is high (>80%)"
            fi
        fi
    else
        print_error "Node health check failed"
    fi
}

# Check dead letter queue
check_dead_letter_queue() {
    print_info "Checking dead letter queue..."
    
    VHOST_ENCODED=$(echo "$RABBITMQ_VHOST" | sed 's/\//%2F/g')
    DLQ_INFO=$(curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASS" "http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/queues/$VHOST_ENCODED/dead-letter.queue")
    
    if echo "$DLQ_INFO" | grep -q '"name":"dead-letter.queue"'; then
        MESSAGES=$(echo "$DLQ_INFO" | grep -o '"messages":[0-9]*' | head -1 | cut -d':' -f2)
        
        if [ "${MESSAGES:-0}" -gt 0 ]; then
            print_warning "Dead letter queue has $MESSAGES messages - investigate failed message processing"
        else
            print_success "Dead letter queue is empty"
        fi
    else
        print_error "Dead letter queue not found"
    fi
}

# Main execution
main() {
    echo "=========================================="
    echo "RabbitMQ Health Check"
    echo "=========================================="
    echo ""
    
    # Run all checks
    check_rabbitmq_accessible || exit 1
    echo ""
    
    check_vhost
    echo ""
    
    check_exchanges
    echo ""
    
    check_queues
    echo ""
    
    check_bindings
    echo ""
    
    check_connections
    echo ""
    
    check_node_health
    echo ""
    
    check_dead_letter_queue
    echo ""
    
    echo "=========================================="
    echo "Health check completed"
    echo "=========================================="
}

# Run main function
main
