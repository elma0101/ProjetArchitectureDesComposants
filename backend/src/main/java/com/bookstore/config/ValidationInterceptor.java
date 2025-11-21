package com.bookstore.config;

import com.bookstore.validation.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

/**
 * Interceptor for additional validation and security checks
 */
@Component
public class ValidationInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        
        // Skip validation for health check and actuator endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator") || requestURI.equals("/health")) {
            return true;
        }
        
        try {
            // Validate request parameters for potential security threats
            if (!validateRequestParameters(request)) {
                logger.warn("Potentially malicious request detected from IP: {} for URI: {}", 
                           getClientIpAddress(request), requestURI);
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid request parameters\",\"message\":\"Request contains potentially harmful content\"}");
                return false;
            }
            
            // Validate request headers
            if (!validateRequestHeaders(request)) {
                logger.warn("Suspicious request headers detected from IP: {} for URI: {}", 
                           getClientIpAddress(request), requestURI);
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid request headers\",\"message\":\"Request headers contain suspicious content\"}");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error during request validation", e);
            return false;
        }
        
        return true;
    }
    
    private boolean validateRequestParameters(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            
            if (paramValues != null) {
                for (String paramValue : paramValues) {
                    if (!ValidationUtils.isSafeString(paramValue)) {
                        logger.debug("Unsafe parameter detected: {} = {}", paramName, paramValue);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean validateRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Skip common headers that might contain special characters
            if (isSkippableHeader(headerName)) {
                continue;
            }
            
            if (headerValue != null && !ValidationUtils.isSafeString(headerValue)) {
                logger.debug("Unsafe header detected: {} = {}", headerName, headerValue);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isSkippableHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.equals("user-agent") ||
               lowerHeaderName.equals("accept") ||
               lowerHeaderName.equals("accept-encoding") ||
               lowerHeaderName.equals("accept-language") ||
               lowerHeaderName.equals("authorization") ||
               lowerHeaderName.equals("content-type") ||
               lowerHeaderName.equals("cookie") ||
               lowerHeaderName.equals("referer") ||
               lowerHeaderName.startsWith("x-");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}