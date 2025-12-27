package com.bookstore.loanmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to validate service-to-service authentication using API keys
 */
@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthenticationFilter.class);
    private static final String SERVICE_API_KEY_HEADER = "X-Service-API-Key";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    
    @Value("${security.service.api-key}")
    private String expectedApiKey;
    
    @Value("${security.service.allowed-services}")
    private String allowedServicesConfig;
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/actuator/health",
        "/actuator/info"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check for service authentication headers
        String apiKey = request.getHeader(SERVICE_API_KEY_HEADER);
        String serviceName = request.getHeader(SERVICE_NAME_HEADER);
        
        // If service headers are present, validate them
        if (apiKey != null && serviceName != null) {
            if (!validateServiceAuthentication(apiKey, serviceName)) {
                logger.warn("Invalid service authentication attempt from service: {} for path: {}", serviceName, path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid service credentials\"}");
                return;
            }
            
            logger.debug("Service authentication successful for service: {} accessing path: {}", serviceName, path);
            request.setAttribute("authenticatedService", serviceName);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private boolean validateServiceAuthentication(String apiKey, String serviceName) {
        // Validate API key
        if (!expectedApiKey.equals(apiKey)) {
            return false;
        }
        
        // Validate service name is in allowed list
        List<String> allowedServices = Arrays.asList(allowedServicesConfig.split(","));
        return allowedServices.contains(serviceName);
    }
}
