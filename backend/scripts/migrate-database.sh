#!/bin/bash

# Database Migration Script for Bookstore Application
# This script helps manage Flyway database migrations

set -e

# Default values
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="bookstore"
DB_USER="bookstore_user"
DB_PASSWORD="bookstore_password"
FLYWAY_LOCATIONS="classpath:db/migration"
ACTION=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] ACTION"
    echo ""
    echo "Actions:"
    echo "  migrate     - Run pending migrations"
    echo "  info        - Show migration status"
    echo "  validate    - Validate migrations"
    echo "  baseline    - Baseline existing database"
    echo "  clean       - Clean database (WARNING: Destructive!)"
    echo "  repair      - Repair migration metadata"
    echo ""
    echo "Options:"
    echo "  -h, --host HOST         Database host (default: localhost)"
    echo "  -p, --port PORT         Database port (default: 5432)"
    echo "  -d, --database DB       Database name (default: bookstore)"
    echo "  -u, --user USER         Database user (default: bookstore_user)"
    echo "  -w, --password PASS     Database password (default: bookstore_password)"
    echo "  -l, --locations LOCS    Migration locations (default: classpath:db/migration)"
    echo "  --help                  Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD can be used instead of options"
    echo ""
    echo "Examples:"
    echo "  $0 migrate"
    echo "  $0 -h prod-db.example.com -d bookstore_prod migrate"
    echo "  $0 info"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -w|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -l|--locations)
            FLYWAY_LOCATIONS="$2"
            shift 2
            ;;
        --help)
            show_usage
            exit 0
            ;;
        migrate|info|validate|baseline|clean|repair)
            ACTION="$1"
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Check if action is provided
if [[ -z "$ACTION" ]]; then
    print_error "No action specified"
    show_usage
    exit 1
fi

# Use environment variables if set
DB_HOST=${DB_HOST:-$DB_HOST}
DB_PORT=${DB_PORT:-$DB_PORT}
DB_NAME=${DB_NAME:-$DB_NAME}
DB_USER=${DB_USER:-$DB_USER}
DB_PASSWORD=${DB_PASSWORD:-$DB_PASSWORD}

# Construct database URL
DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"

print_info "Database: $DB_URL"
print_info "User: $DB_USER"
print_info "Action: $ACTION"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    exit 1
fi

# Check if we're in the correct directory
if [[ ! -f "pom.xml" ]]; then
    print_error "pom.xml not found. Please run this script from the backend directory."
    exit 1
fi

# Set Maven properties
MAVEN_PROPS="-Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD -Dflyway.locations=$FLYWAY_LOCATIONS"

# Execute the action
case $ACTION in
    migrate)
        print_info "Running database migrations..."
        mvn flyway:migrate $MAVEN_PROPS
        if [[ $? -eq 0 ]]; then
            print_info "Migrations completed successfully"
        else
            print_error "Migration failed"
            exit 1
        fi
        ;;
    info)
        print_info "Getting migration status..."
        mvn flyway:info $MAVEN_PROPS
        ;;
    validate)
        print_info "Validating migrations..."
        mvn flyway:validate $MAVEN_PROPS
        if [[ $? -eq 0 ]]; then
            print_info "Migrations are valid"
        else
            print_error "Migration validation failed"
            exit 1
        fi
        ;;
    baseline)
        print_info "Baselining database..."
        mvn flyway:baseline $MAVEN_PROPS
        if [[ $? -eq 0 ]]; then
            print_info "Database baselined successfully"
        else
            print_error "Baseline failed"
            exit 1
        fi
        ;;
    clean)
        print_warning "This will DELETE ALL DATA in the database!"
        read -p "Are you sure you want to continue? (yes/no): " confirm
        if [[ $confirm == "yes" ]]; then
            print_info "Cleaning database..."
            mvn flyway:clean $MAVEN_PROPS
            if [[ $? -eq 0 ]]; then
                print_info "Database cleaned successfully"
            else
                print_error "Clean failed"
                exit 1
            fi
        else
            print_info "Clean operation cancelled"
        fi
        ;;
    repair)
        print_info "Repairing migration metadata..."
        mvn flyway:repair $MAVEN_PROPS
        if [[ $? -eq 0 ]]; then
            print_info "Repair completed successfully"
        else
            print_error "Repair failed"
            exit 1
        fi
        ;;
    *)
        print_error "Unknown action: $ACTION"
        show_usage
        exit 1
        ;;
esac

print_info "Operation completed"