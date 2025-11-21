package com.bookstore.versioning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for API versioning components
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ApiVersioningIntegrationTest {

    @Autowired
    private FeatureToggleService featureToggleService;

    @Test
    void testFeatureToggleServiceIntegration() {
        assertNotNull(featureToggleService);
        
        // Test that the service is properly configured
        assertTrue(featureToggleService.isFeatureEnabled("enhanced-search"));
        assertTrue(featureToggleService.isFeatureEnabled("bulk-operations"));
        
        // Test runtime toggling
        featureToggleService.disableFeature("test-feature");
        assertFalse(featureToggleService.isFeatureEnabled("test-feature"));
        
        featureToggleService.enableFeature("test-feature");
        assertTrue(featureToggleService.isFeatureEnabled("test-feature"));
    }

    @Test
    void testApiVersionConditionLogic() {
        ApiVersionCondition condition = new ApiVersionCondition("1.0");
        assertNotNull(condition);
        assertEquals("1.0", condition.getVersion());
        
        // Test combining conditions
        ApiVersionCondition methodCondition = new ApiVersionCondition("2.0");
        ApiVersionCondition combined = condition.combine(methodCondition);
        assertEquals("2.0", combined.getVersion());
    }

    @Test
    void testVersioningConfiguration() {
        // Test that the versioning configuration is properly loaded
        assertNotNull(featureToggleService);
        
        // Verify default feature states
        var features = featureToggleService.getAllFeatureStates();
        assertNotNull(features);
        assertTrue(features.size() > 0);
    }
}