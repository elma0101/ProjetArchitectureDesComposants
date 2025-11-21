package com.bookstore.versioning;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage feature toggles
 */
@Service
public class FeatureToggleService {

    private final Map<String, Boolean> featureToggles = new ConcurrentHashMap<>();
    
    @Value("${app.features.enhanced-search:true}")
    private boolean enhancedSearchEnabled;
    
    @Value("${app.features.advanced-recommendations:true}")
    private boolean advancedRecommendationsEnabled;
    
    @Value("${app.features.bulk-operations:true}")
    private boolean bulkOperationsEnabled;
    
    @Value("${app.features.loan-analytics:true}")
    private boolean loanAnalyticsEnabled;

    public boolean isFeatureEnabled(String featureName) {
        return featureToggles.computeIfAbsent(featureName, this::getDefaultFeatureState);
    }

    public void enableFeature(String featureName) {
        featureToggles.put(featureName, true);
    }

    public void disableFeature(String featureName) {
        featureToggles.put(featureName, false);
    }

    public Map<String, Boolean> getAllFeatureStates() {
        Map<String, Boolean> allFeatures = new ConcurrentHashMap<>();
        
        // Add configured features
        allFeatures.put("enhanced-search", enhancedSearchEnabled);
        allFeatures.put("advanced-recommendations", advancedRecommendationsEnabled);
        allFeatures.put("bulk-operations", bulkOperationsEnabled);
        allFeatures.put("loan-analytics", loanAnalyticsEnabled);
        
        // Add runtime toggles
        allFeatures.putAll(featureToggles);
        
        return allFeatures;
    }

    private boolean getDefaultFeatureState(String featureName) {
        return switch (featureName) {
            case "enhanced-search" -> enhancedSearchEnabled;
            case "advanced-recommendations" -> advancedRecommendationsEnabled;
            case "bulk-operations" -> bulkOperationsEnabled;
            case "loan-analytics" -> loanAnalyticsEnabled;
            default -> true; // Default to enabled for unknown features
        };
    }
}