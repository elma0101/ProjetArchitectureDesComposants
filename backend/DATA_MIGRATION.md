# Data Migration and Management

This document describes the data migration, backup, and seeding functionality implemented in the bookstore application.

## Overview

The application provides comprehensive data management capabilities including:

- **Database Migrations**: Using Flyway for version-controlled schema changes
- **Data Seeding**: Automatic population of test data for development/testing
- **Data Export/Import**: JSON-based data export and import functionality
- **Backup/Restore**: Database and application data backup and restore procedures

## Database Migrations with Flyway

### Configuration

Flyway is configured in `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
    locations: classpath:db/migration
    baseline-version: 0
    baseline-description: "Initial baseline"
```

### Migration Files

Migration files are located in `src/main/resources/db/migration/`:

- `V1__Create_initial_schema.sql` - Creates all tables, indexes, and constraints
- `V2__Insert_sample_data.sql` - Inserts initial sample data

### Running Migrations

#### Using Maven Plugin

```bash
# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Baseline existing database
mvn flyway:baseline
```

#### Using the Migration Script

```bash
# Make script executable
chmod +x scripts/migrate-database.sh

# Run migrations
./scripts/migrate-database.sh migrate

# Check status
./scripts/migrate-database.sh info

# Validate migrations
./scripts/migrate-database.sh validate
```

#### Script Options

```bash
./scripts/migrate-database.sh [OPTIONS] ACTION

Options:
  -h, --host HOST         Database host (default: localhost)
  -p, --port PORT         Database port (default: 5432)
  -d, --database DB       Database name (default: bookstore)
  -u, --user USER         Database user (default: bookstore_user)
  -w, --password PASS     Database password
  -l, --locations LOCS    Migration locations

Actions:
  migrate     - Run pending migrations
  info        - Show migration status
  validate    - Validate migrations
  baseline    - Baseline existing database
  clean       - Clean database (WARNING: Destructive!)
  repair      - Repair migration metadata
```

## Data Seeding

### DataSeedingService

The `DataSeedingService` automatically seeds the database with test data when running in `dev` or `test` profiles.

#### Features

- Runs on application startup via `CommandLineRunner`
- Only seeds data if database is empty
- Creates realistic test data for all entities
- Maintains referential integrity
- Encrypts user passwords

#### Seeded Data

- **8 Authors**: Classic and contemporary authors
- **13 Books**: Various genres with proper author relationships
- **4 Users**: Admin, librarian, and regular users with different roles
- **6 Loans**: Mix of active, returned, and overdue loans
- **10 Recommendations**: Different recommendation types
- **Loan Tracking**: Historical tracking events

### Manual Seeding

```java
@Autowired
private DataSeedingService dataSeedingService;

// Manually trigger seeding
dataSeedingService.run();
```

## Data Export and Import

### DataExportImportService

Provides comprehensive data export and import functionality.

#### Export Features

- **Full Export**: All application data in compressed ZIP format
- **Entity Export**: Specific entity types in JSON format
- **Password Security**: User passwords are redacted in exports
- **Metadata**: Export includes statistics and metadata

#### Import Features

- **Full Import**: Restore all data from ZIP files
- **Entity Import**: Import specific entity types from JSON
- **Validation**: File integrity validation
- **Relationship Preservation**: Maintains entity relationships

### API Endpoints

All endpoints require `ADMIN` role and are prefixed with `/api/admin/data`:

#### Export Endpoints

```http
POST /export/all
POST /export/{entityType}
GET /export/statistics
```

#### Import Endpoints

```http
POST /import/all?filePath={path}
POST /import/{entityType}?filePath={path}
POST /import/upload (with multipart file)
```

#### Validation

```http
POST /validate?filePath={path}
```

### Usage Examples

```bash
# Export all data
curl -X POST http://localhost:8080/api/admin/data/export/all \
  -H "Authorization: Bearer {token}"

# Export specific entity
curl -X POST http://localhost:8080/api/admin/data/export/authors \
  -H "Authorization: Bearer {token}"

# Import data
curl -X POST "http://localhost:8080/api/admin/data/import/all?filePath=/path/to/export.zip" \
  -H "Authorization: Bearer {token}"

# Get export statistics
curl -X GET http://localhost:8080/api/admin/data/export/statistics \
  -H "Authorization: Bearer {token}"
```

## Backup and Restore

### BackupRestoreService

Provides database and application data backup/restore capabilities.

#### Backup Types

1. **Database Backup**: Uses `pg_dump` for PostgreSQL database dumps
2. **Application Data Backup**: Uses export service for JSON data
3. **Full Backup**: Combines both database and application data backups

#### Restore Types

1. **Database Restore**: Uses `psql` to restore from SQL dumps
2. **Application Data Restore**: Uses import service for JSON data

#### Scheduled Backups

- Automatic daily backups at 2 AM
- Configurable via `@Scheduled` annotation
- Automatic cleanup of old backups (30+ days)

### API Endpoints

#### Backup Endpoints

```http
POST /backup/database
POST /backup/data
POST /backup/full
```

#### Restore Endpoints

```http
POST /restore/database?backupFilePath={path}
POST /restore/data?backupFilePath={path}
```

#### Management Endpoints

```http
GET /backups
POST /cleanup
```

### Prerequisites

For database backup/restore functionality:

1. **PostgreSQL Client Tools**: `pg_dump` and `psql` must be installed
2. **Environment Variables**: Set `PGPASSWORD` or use `.pgpass` file
3. **Network Access**: Database server must be accessible

### Usage Examples

```bash
# Create database backup
curl -X POST http://localhost:8080/api/admin/data/backup/database \
  -H "Authorization: Bearer {token}"

# Create full backup
curl -X POST http://localhost:8080/api/admin/data/backup/full \
  -H "Authorization: Bearer {token}"

# List available backups
curl -X GET http://localhost:8080/api/admin/data/backups \
  -H "Authorization: Bearer {token}"

# Restore from backup
curl -X POST "http://localhost:8080/api/admin/data/restore/data?backupFilePath=/path/to/backup.zip" \
  -H "Authorization: Bearer {token}"
```

## File Locations

### Export Files

- **Directory**: `exports/` (created automatically)
- **Format**: `{type}_export_{timestamp}.{extension}`
- **Examples**:
  - `bookstore_export_20241117_120000.zip`
  - `authors_export_20241117_120000.json`

### Backup Files

- **Directory**: `backups/` (created automatically)
- **Format**: `{type}_backup_{timestamp}.{extension}`
- **Examples**:
  - `database_backup_20241117_120000.sql`
  - `bookstore_export_20241117_120000.zip`

## Security Considerations

### Access Control

- All management endpoints require `ADMIN` role
- JWT authentication required for all operations
- Rate limiting applied to prevent abuse

### Data Protection

- User passwords are automatically redacted in exports
- Sensitive data is not logged in plain text
- File validation prevents malicious imports

### Audit Trail

- All operations are logged with timestamps
- User actions are tracked in audit logs
- Failed operations are logged with error details

## Monitoring and Alerting

### Metrics

- Backup success/failure rates
- Export/import operation durations
- File sizes and record counts
- Error rates and types

### Health Checks

- Database connectivity
- File system availability
- Backup schedule status

### Logging

- Structured logging with correlation IDs
- Different log levels for different operations
- Centralized logging for production environments

## Troubleshooting

### Common Issues

1. **Migration Failures**
   - Check database connectivity
   - Verify user permissions
   - Review migration file syntax

2. **Backup Failures**
   - Ensure PostgreSQL client tools are installed
   - Check database credentials
   - Verify disk space availability

3. **Import Failures**
   - Validate file format and integrity
   - Check for data conflicts
   - Review constraint violations

### Recovery Procedures

1. **Failed Migration**
   ```bash
   # Check migration status
   ./scripts/migrate-database.sh info
   
   # Repair if needed
   ./scripts/migrate-database.sh repair
   
   # Re-run migration
   ./scripts/migrate-database.sh migrate
   ```

2. **Corrupted Data**
   ```bash
   # Restore from latest backup
   curl -X POST "http://localhost:8080/api/admin/data/restore/data?backupFilePath=/path/to/backup.zip"
   ```

3. **Database Issues**
   ```bash
   # Clean and rebuild (DESTRUCTIVE!)
   ./scripts/migrate-database.sh clean
   ./scripts/migrate-database.sh migrate
   ```

## Best Practices

### Development

1. Always test migrations on a copy of production data
2. Use descriptive migration file names and comments
3. Keep migrations small and focused
4. Never modify existing migration files

### Production

1. Schedule regular backups during low-traffic periods
2. Test restore procedures regularly
3. Monitor backup success rates
4. Keep multiple backup copies in different locations

### Data Management

1. Validate exports before using for imports
2. Use entity-specific exports for partial data migration
3. Clean up old export/backup files regularly
4. Document any manual data changes

## Configuration Reference

### Application Properties

```yaml
# Flyway Configuration
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
    locations: classpath:db/migration
    baseline-version: 0
    clean-disabled: true

# Async Configuration
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100
```

### Environment Variables

- `DB_HOST`: Database host
- `DB_PORT`: Database port
- `DB_NAME`: Database name
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `PGPASSWORD`: PostgreSQL password for command-line tools

## Testing

### Unit Tests

- `DataSeedingServiceTest`: Tests data seeding functionality
- `DataExportImportServiceTest`: Tests export/import operations
- `BackupRestoreServiceTest`: Tests backup/restore operations
- `DataManagementControllerTest`: Tests REST API endpoints

### Integration Tests

- `DataMigrationIntegrationTest`: End-to-end data migration testing
- Tests complete workflows including seeding, export, and import
- Validates data integrity and relationship preservation

### Running Tests

```bash
# Run all data migration tests
mvn test -Dtest="*DataMigration*,*DataSeeding*,*DataExportImport*,*BackupRestore*"

# Run integration tests
mvn test -Dtest="DataMigrationIntegrationTest"
```