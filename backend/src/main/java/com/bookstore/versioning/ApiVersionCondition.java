package com.bookstore.versioning;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Custom RequestCondition for API versioning
 */
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    private final String version;
    
    public static final String VERSION_HEADER = "API-Version";
    public static final String DEFAULT_VERSION = "1.0";

    public ApiVersionCondition(String version) {
        this.version = version;
    }

    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        // Method-level annotation takes precedence over class-level
        return new ApiVersionCondition(other.version);
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        String requestVersion = extractVersion(request);
        
        // If no version specified in request, use default
        if (requestVersion == null) {
            requestVersion = DEFAULT_VERSION;
        }
        
        // Check if the requested version matches this condition
        if (isVersionCompatible(requestVersion, this.version)) {
            return this;
        }
        
        return null;
    }

    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        // Higher versions have higher priority
        return compareVersions(other.version, this.version);
    }

    private String extractVersion(HttpServletRequest request) {
        // Try header first
        String headerVersion = request.getHeader(VERSION_HEADER);
        if (headerVersion != null) {
            return headerVersion;
        }
        
        // Try URL path parameter
        String path = request.getRequestURI();
        if (path.startsWith("/api/v")) {
            int start = path.indexOf("/api/v") + 6;
            int end = path.indexOf("/", start);
            if (end == -1) end = path.length();
            try {
                return path.substring(start, end);
            } catch (StringIndexOutOfBoundsException e) {
                return null;
            }
        }
        
        return null;
    }

    private boolean isVersionCompatible(String requestedVersion, String supportedVersion) {
        // Exact match
        if (Objects.equals(requestedVersion, supportedVersion)) {
            return true;
        }
        
        // Backward compatibility: newer API versions can handle older requests
        return compareVersions(supportedVersion, requestedVersion) >= 0;
    }

    private int compareVersions(String version1, String version2) {
        if (version1 == null && version2 == null) return 0;
        if (version1 == null) return -1;
        if (version2 == null) return 1;
        
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }
        
        return 0;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ApiVersionCondition that = (ApiVersionCondition) obj;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return "ApiVersionCondition{version='" + version + "'}";
    }
}