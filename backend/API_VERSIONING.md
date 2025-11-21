# API Versioning and Backward Compatibility Guide

## Overview

The Bookstore API implements a comprehensive versioning strategy to ensure backward compatibility while allowing for feature evolution. This document outlines the versioning approach, migration strategies, and feature toggle mechanisms.

## Versioning Strategy

### Version Specification Methods

The API supports multiple ways to specify the desired version:

1. **URL Path Versioning** (Recommended)
   ```
   GET /api/v1/books
   GET /api/v2/books
   ```

2. **Header-based Versioning**
   ```
   GET /api/books
   API-Version: 1.0
   ```

3. **Default Version**
   - If no version is specified, the API defaults to version 1.0
   - This ensures backward compatibility for existing clients

### Version Compatibility

- **Forward Compatibility**: Newer API versions can handle requests from older clients
- **Backward Compatibility**: Maintained for at least 2 major versions
- **Graceful Degradation**: Missing features in older versions return appropriate responses

## API Versions

### Version 1.0 (Current Stable)
- **Endpoints**: `/api/v1/*`
- **Features**: Basic CRUD operations for books, authors, and loans
- **Status**: Stable, fully supported
- **Deprecation**: None planned

### Version 2.0 (Enhanced)
- **Endpoints**: `/api/v2/*`
- **Features**: 
  - Enhanced search capabilities
  - Bulk operations
  - Advanced filtering
  - Improved DTOs with validation
- **Status**: Stable, recommended for new integrations
- **Backward Compatibility**: Supports v1.0 requests

### Version 0.9 (Legacy - Deprecated)
- **Endpoints**: `/api/legacy/*`
- **Status**: Deprecated since 2024-01-01
- **Migration Guide**: https://docs.bookstore.com/migration/v1
- **Sunset Date**: 2024-12-31

## Deprecation Process

### Deprecation Headers

When accessing deprecated endpoints, the following headers are returned:

```http
Deprecation: true
API-Deprecated-Version: 0.9
API-Deprecated-Since: 2024-01-01
API-Migration-Guide: https://docs.bookstore.com/migration/v1
Warning: 299 - "This API version is deprecated. Please migrate to a newer version."
```

### Deprecation Timeline

1. **Announcement**: 6 months before deprecation
2. **Deprecation**: API marked as deprecated, warnings added
3. **Sunset**: 12 months after deprecation announcement
4. **Removal**: API endpoints removed

## Feature Toggles

### Available Features

The API supports runtime feature toggles for gradual rollouts:

- `enhanced-search`: Advanced search capabilities
- `advanced-recommendations`: ML-based recommendations
- `bulk-operations`: Bulk create/update operations
- `loan-analytics`: Advanced loan analytics

### Feature Toggle Management

#### Check Feature Status
```http
GET /api/admin/features
```

#### Enable Feature
```http
POST /api/admin/features/{featureName}/enable
```

#### Disable Feature
```http
POST /api/admin/features/{featureName}/disable
```

### Configuration

Features can be configured via application properties:

```yaml
app:
  features:
    enhanced-search: true
    advanced-recommendations: true
    bulk-operations: false
    loan-analytics: true
```

## Migration Guides

### Migrating from v0.9 to v1.0

#### Breaking Changes
- Response format changed from simple arrays to paginated responses
- Error response structure standardized
- Authentication requirements added for admin endpoints

#### Migration Steps
1. Update response parsing to handle pagination
2. Update error handling for new error format
3. Add authentication headers for admin operations

#### Example Changes

**Before (v0.9):**
```json
GET /api/legacy/books
Response: [{"id": 1, "title": "Book 1"}, ...]
```

**After (v1.0):**
```json
GET /api/v1/books
Response: {
  "content": [{"id": 1, "title": "Book 1"}, ...],
  "pageable": {...},
  "totalElements": 100
}
```

### Migrating from v1.0 to v2.0

#### New Features
- Enhanced search with multiple parameters
- Bulk operations support
- Improved validation with detailed error messages
- Feature toggle support

#### Backward Compatibility
- All v1.0 endpoints remain functional
- v2.0 endpoints accept v1.0 requests
- Response format maintains compatibility

#### Recommended Upgrades
1. Use v2.0 endpoints for new features
2. Migrate to enhanced DTOs for better validation
3. Implement bulk operations for better performance

## Error Handling

### Version-Specific Errors

#### Unsupported Version
```http
HTTP/1.1 406 Not Acceptable
Content-Type: application/json

{
  "error": "Unsupported API version",
  "message": "API version 3.0 is not supported",
  "supportedVersions": ["1.0", "2.0"]
}
```

#### Feature Disabled
```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "error": "Feature disabled",
  "message": "The feature 'bulk-operations' is currently disabled",
  "code": "FEATURE_DISABLED"
}
```

## Best Practices

### For API Consumers

1. **Always specify version**: Use URL path versioning for clarity
2. **Handle deprecation warnings**: Monitor response headers for deprecation notices
3. **Implement graceful degradation**: Handle feature unavailability gracefully
4. **Stay updated**: Regularly check for new versions and migration guides

### For API Providers

1. **Maintain backward compatibility**: Support previous versions for reasonable periods
2. **Provide clear migration paths**: Document breaking changes and migration steps
3. **Use feature toggles**: Enable gradual rollouts and quick rollbacks
4. **Monitor usage**: Track version usage to plan deprecation timelines

## Testing Strategy

### Version Compatibility Tests
- Cross-version compatibility testing
- Backward compatibility validation
- Feature toggle integration tests
- Deprecation warning verification

### Test Categories
1. **Unit Tests**: Version condition logic, feature toggle service
2. **Integration Tests**: End-to-end version negotiation
3. **Contract Tests**: API contract compliance across versions
4. **Performance Tests**: Version overhead measurement

## Monitoring and Analytics

### Metrics to Track
- Version usage distribution
- Deprecated endpoint usage
- Feature toggle activation rates
- Migration completion rates

### Alerting
- High usage of deprecated endpoints
- Feature toggle failures
- Version negotiation errors

## Support and Documentation

### Resources
- API Documentation: `/swagger-ui.html`
- Migration Guides: `/docs/migration/`
- Feature Toggle Status: `/api/admin/features`
- Health Check: `/actuator/health`

### Support Channels
- Technical Documentation: Internal wiki
- Migration Support: Development team
- Feature Requests: Product management

## Conclusion

The API versioning strategy ensures smooth evolution of the Bookstore API while maintaining backward compatibility. By following the guidelines in this document, both API consumers and providers can effectively manage version transitions and feature rollouts.