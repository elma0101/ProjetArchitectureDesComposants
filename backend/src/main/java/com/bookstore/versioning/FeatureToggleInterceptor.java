package com.bookstore.versioning;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Interceptor to handle feature toggles
 */
public class FeatureToggleInterceptor implements HandlerInterceptor {

    private final FeatureToggleService featureToggleService;

    public FeatureToggleInterceptor(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (handler instanceof HandlerMethod handlerMethod) {
            // Check method-level feature toggle first
            FeatureToggle methodToggle = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), FeatureToggle.class);
            if (methodToggle != null && !featureToggleService.isFeatureEnabled(methodToggle.value())) {
                sendFeatureDisabledResponse(response, methodToggle.value());
                return false;
            }
            
            // Check class-level feature toggle
            FeatureToggle classToggle = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), FeatureToggle.class);
            if (classToggle != null && !featureToggleService.isFeatureEnabled(classToggle.value())) {
                sendFeatureDisabledResponse(response, classToggle.value());
                return false;
            }
        }
        
        return true;
    }

    private void sendFeatureDisabledResponse(HttpServletResponse response, String featureName) throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
            "{\"error\":\"Feature disabled\",\"message\":\"The feature '%s' is currently disabled\",\"code\":\"FEATURE_DISABLED\"}", 
            featureName
        ));
    }
}