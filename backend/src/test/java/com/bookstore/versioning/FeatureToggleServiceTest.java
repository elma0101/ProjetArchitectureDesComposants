package com.bookstore.versioning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureToggleService
 */
class FeatureToggleServiceTest {

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService();
        
        // Set default values using reflection to simulate @Value injection
        ReflectionTestUtils.setField(featureToggleService, "enhancedSearchEnabled", true);
        ReflectionTestUtils.setField(featureToggleService, "advancedRecommendationsEnabled", true);
        ReflectionTestUtils.setField(featureToggleService, "bulkOperationsEnabled", true);
        ReflectionTestUtils.setField(featureToggleService, "loanAnalyticsEnabled", true);
    }

    @Test
    void testDefaultFeatureStates() {
        assertTrue(featureToggleService.isFeatureEnabled("enhanced-search"));
        assertTrue(featureToggleService.isFeatureEnabled("advanced-recommendations"));
        assertTrue(featureToggleService.isFeatureEnabled("bulk-operations"));
        assertTrue(featureToggleService.isFeatureEnabled("loan-analytics"));
    }

    @Test
    void testEnableFeature() {
        featureToggleService.disableFeature("enhanced-search");
        assertFalse(featureToggleService.isFeatureEnabled("enhanced-search"));
        
        featureToggleService.enableFeature("enhanced-search");
        assertTrue(featureToggleService.isFeatureEnabled("enhanced-search"));
    }

    @Test
    void testDisableFeature() {
        assertTrue(featureToggleService.isFeatureEnabled("bulk-operations"));
        
        featureToggleService.disableFeature("bulk-operations");
        assertFalse(featureToggleService.isFeatureEnabled("bulk-operations"));
    }

    @Test
    void testUnknownFeatureDefaultsToEnabled() {
        assertTrue(featureToggleService.isFeatureEnabled("unknown-feature"));
    }

    @Test
    void testGetAllFeatureStates() {
        Map<String, Boolean> features = featureToggleService.getAllFeatureStates();
        
        assertNotNull(features);
        assertTrue(features.containsKey("enhanced-search"));
        assertTrue(features.containsKey("advanced-recommendations"));
        assertTrue(features.containsKey("bulk-operations"));
        assertTrue(features.containsKey("loan-analytics"));
        
        // All should be enabled by default
        assertTrue(features.get("enhanced-search"));
        assertTrue(features.get("advanced-recommendations"));
        assertTrue(features.get("bulk-operations"));
        assertTrue(features.get("loan-analytics"));
    }

    @Test
    void testRuntimeToggleOverridesDefault() {
        // Default is enabled
        assertTrue(featureToggleService.isFeatureEnabled("enhanced-search"));
        
        // Runtime disable
        featureToggleService.disableFeature("enhanced-search");
        assertFalse(featureToggleService.isFeatureEnabled("enhanced-search"));
        
        // Check that it appears in getAllFeatureStates
        Map<String, Boolean> features = featureToggleService.getAllFeatureStates();
        assertFalse(features.get("enhanced-search"));
    }

    @Test
    void testFeatureTogglePersistence() {
        // Toggle a feature multiple times
        featureToggleService.disableFeature("test-feature");
        assertFalse(featureToggleService.isFeatureEnabled("test-feature"));
        
        featureToggleService.enableFeature("test-feature");
        assertTrue(featureToggleService.isFeatureEnabled("test-feature"));
        
        featureToggleService.disableFeature("test-feature");
        assertFalse(featureToggleService.isFeatureEnabled("test-feature"));
    }
}