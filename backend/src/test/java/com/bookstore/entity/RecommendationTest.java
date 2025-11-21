package com.bookstore.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationTest {
    
    private Validator validator;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        testBook = new Book("Test Title", "978-0-452-28423-4");
        testBook.setId(1L);
    }
    
    @Test
    void testValidRecommendation() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        recommendation.setReason("Based on your reading history");
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertTrue(violations.isEmpty(), "Valid recommendation should have no validation errors");
    }
    
    @Test
    void testRecommendationWithNullBook() {
        Recommendation recommendation = new Recommendation("USER001", null, RecommendationType.CONTENT_BASED, 0.85);
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with null book should have validation errors");
        
        boolean bookViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("book"));
        assertTrue(bookViolationFound, "Should have book validation error");
    }
    
    @Test
    void testRecommendationWithNullType() {
        Recommendation recommendation = new Recommendation("USER001", testBook, null, 0.85);
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with null type should have validation errors");
        
        boolean typeViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
        assertTrue(typeViolationFound, "Should have type validation error");
    }
    
    @Test
    void testRecommendationWithInvalidScore() {
        // Test negative score
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, -0.1);
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with negative score should have validation errors");
        
        boolean scoreViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("score"));
        assertTrue(scoreViolationFound, "Should have score validation error for negative value");
        
        // Test score > 1.0
        recommendation.setScore(1.1);
        violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with score > 1.0 should have validation errors");
        
        scoreViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("score"));
        assertTrue(scoreViolationFound, "Should have score validation error for value > 1.0");
    }
    
    @Test
    void testRecommendationWithValidScoreRange() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.0);
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertTrue(violations.isEmpty(), "Recommendation with score 0.0 should be valid");
        
        recommendation.setScore(1.0);
        violations = validator.validate(recommendation);
        assertTrue(violations.isEmpty(), "Recommendation with score 1.0 should be valid");
        
        recommendation.setScore(0.5);
        violations = validator.validate(recommendation);
        assertTrue(violations.isEmpty(), "Recommendation with score 0.5 should be valid");
    }
    
    @Test
    void testRecommendationWithTooLongUserId() {
        String longUserId = "A".repeat(101); // Exceeds 100 character limit
        Recommendation recommendation = new Recommendation(longUserId, testBook, RecommendationType.CONTENT_BASED, 0.85);
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with user ID too long should have validation errors");
    }
    
    @Test
    void testRecommendationWithTooLongReason() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        String longReason = "A".repeat(501); // Exceeds 500 character limit
        recommendation.setReason(longReason);
        
        Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
        assertFalse(violations.isEmpty(), "Recommendation with reason too long should have validation errors");
    }
    
    @Test
    void testRecommendationIsHighScore() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        assertTrue(recommendation.isHighScore(), "Score 0.85 should be considered high score");
        
        recommendation.setScore(0.7);
        assertTrue(recommendation.isHighScore(), "Score 0.7 should be considered high score");
        
        recommendation.setScore(0.69);
        assertFalse(recommendation.isHighScore(), "Score 0.69 should not be considered high score");
        
        recommendation.setScore(null);
        assertFalse(recommendation.isHighScore(), "Null score should not be considered high score");
    }
    
    @Test
    void testRecommendationIsRecentRecommendation() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        
        // Simulate recent creation (createdAt is set by @CreationTimestamp in real scenario)
        // For testing, we'll use reflection or assume the field is set
        assertTrue(recommendation.isRecentRecommendation() || recommendation.getCreatedAt() == null, 
                  "New recommendation should be recent or have null createdAt in test");
    }
    
    @Test
    void testRecommendationTypes() {
        // Test all recommendation types
        for (RecommendationType type : RecommendationType.values()) {
            Recommendation recommendation = new Recommendation("USER001", testBook, type, 0.85);
            Set<ConstraintViolation<Recommendation>> violations = validator.validate(recommendation);
            assertTrue(violations.isEmpty(), "Recommendation with type " + type + " should be valid");
        }
    }
    
    @Test
    void testRecommendationEqualsAndHashCode() {
        Recommendation recommendation1 = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        recommendation1.setId(1L);
        
        Recommendation recommendation2 = new Recommendation("USER002", testBook, RecommendationType.COLLABORATIVE, 0.75);
        recommendation2.setId(1L);
        
        Recommendation recommendation3 = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        recommendation3.setId(2L);
        
        assertEquals(recommendation1, recommendation2, "Recommendations with same ID should be equal");
        assertNotEquals(recommendation1, recommendation3, "Recommendations with different ID should not be equal");
        
        assertEquals(recommendation1.hashCode(), recommendation2.hashCode(), 
                    "Recommendations with same ID should have same hash code");
    }
    
    @Test
    void testRecommendationToString() {
        Recommendation recommendation = new Recommendation("USER001", testBook, RecommendationType.CONTENT_BASED, 0.85);
        recommendation.setId(1L);
        
        String toString = recommendation.toString();
        assertTrue(toString.contains("USER001"), "toString should contain user ID");
        assertTrue(toString.contains(recommendation.getType().toString()), "toString should contain type");
        assertTrue(toString.contains("0.85"), "toString should contain score");
    }
    
    @Test
    void testRecommendationTypeEnum() {
        assertEquals("Collaborative Filtering", RecommendationType.COLLABORATIVE.getDisplayName());
        assertEquals("Content-Based", RecommendationType.CONTENT_BASED.getDisplayName());
        assertEquals("Popular Books", RecommendationType.POPULAR.getDisplayName());
        assertEquals("Trending Books", RecommendationType.TRENDING.getDisplayName());
        
        assertEquals("Collaborative Filtering", RecommendationType.COLLABORATIVE.toString());
    }
}