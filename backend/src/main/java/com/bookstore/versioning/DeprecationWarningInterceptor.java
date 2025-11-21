package com.bookstore.versioning;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor to add deprecation warnings to API responses
 */
public class DeprecationWarningInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            // Check method-level annotation first
            ApiVersion methodVersion = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), ApiVersion.class);
            if (methodVersion != null && methodVersion.deprecated()) {
                addDeprecationHeaders(response, methodVersion);
                return true;
            }
            
            // Check class-level annotation
            ApiVersion classVersion = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), ApiVersion.class);
            if (classVersion != null && classVersion.deprecated()) {
                addDeprecationHeaders(response, classVersion);
            }
        }
        
        return true;
    }

    private void addDeprecationHeaders(HttpServletResponse response, ApiVersion apiVersion) {
        response.setHeader("Deprecation", "true");
        response.setHeader("API-Deprecated-Version", apiVersion.value());
        
        if (!apiVersion.deprecatedSince().isEmpty()) {
            response.setHeader("API-Deprecated-Since", apiVersion.deprecatedSince());
        }
        
        if (!apiVersion.migrationGuide().isEmpty()) {
            response.setHeader("API-Migration-Guide", apiVersion.migrationGuide());
        }
        
        response.setHeader("Warning", "299 - \"This API version is deprecated. Please migrate to a newer version.\"");
    }
}