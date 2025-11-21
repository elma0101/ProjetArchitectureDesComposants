package com.bookstore.versioning;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiVersionCondition
 */
class ApiVersionConditionTest {

    @Test
    void testVersionMatching() {
        ApiVersionCondition condition = new ApiVersionCondition("1.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // Test exact version match
        request.addHeader("API-Version", "1.0");
        ApiVersionCondition matchingCondition = condition.getMatchingCondition(request);
        assertNotNull(matchingCondition);
        assertEquals("1.0", matchingCondition.getVersion());
    }

    @Test
    void testUrlPathVersionExtraction() {
        ApiVersionCondition condition = new ApiVersionCondition("1.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1.0/books");
        
        ApiVersionCondition matchingCondition = condition.getMatchingCondition(request);
        assertNotNull(matchingCondition);
    }

    @Test
    void testDefaultVersionFallback() {
        ApiVersionCondition condition = new ApiVersionCondition("1.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No version specified - should use default
        
        ApiVersionCondition matchingCondition = condition.getMatchingCondition(request);
        assertNotNull(matchingCondition);
    }

    @Test
    void testVersionComparison() {
        ApiVersionCondition condition1 = new ApiVersionCondition("1.0");
        ApiVersionCondition condition2 = new ApiVersionCondition("2.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // Higher version should have higher priority (compareTo returns positive when other > this)
        int comparison = condition1.compareTo(condition2, request);
        assertTrue(comparison > 0); // condition2 (2.0) > condition1 (1.0), so comparison should be positive
    }

    @Test
    void testBackwardCompatibility() {
        ApiVersionCondition condition = new ApiVersionCondition("2.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("API-Version", "1.0");
        
        // Version 2.0 should be able to handle 1.0 requests
        ApiVersionCondition matchingCondition = condition.getMatchingCondition(request);
        assertNotNull(matchingCondition);
    }

    @Test
    void testVersionConditionCombine() {
        ApiVersionCondition classLevel = new ApiVersionCondition("1.0");
        ApiVersionCondition methodLevel = new ApiVersionCondition("2.0");
        
        // Method-level should take precedence
        ApiVersionCondition combined = classLevel.combine(methodLevel);
        assertEquals("2.0", combined.getVersion());
    }

    @Test
    void testVersionConditionEquality() {
        ApiVersionCondition condition1 = new ApiVersionCondition("1.0");
        ApiVersionCondition condition2 = new ApiVersionCondition("1.0");
        ApiVersionCondition condition3 = new ApiVersionCondition("2.0");
        
        assertEquals(condition1, condition2);
        assertNotEquals(condition1, condition3);
        assertEquals(condition1.hashCode(), condition2.hashCode());
    }
}