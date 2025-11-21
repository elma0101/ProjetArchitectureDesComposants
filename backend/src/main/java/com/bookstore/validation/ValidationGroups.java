package com.bookstore.validation;

/**
 * Validation groups for different REST operations
 */
public class ValidationGroups {
    
    /**
     * Validation group for entity creation operations (POST)
     */
    public interface Create {}
    
    /**
     * Validation group for entity update operations (PUT/PATCH)
     */
    public interface Update {}
    
    /**
     * Validation group for partial update operations (PATCH)
     */
    public interface PartialUpdate {}
    
    /**
     * Validation group for search operations
     */
    public interface Search {}
    
    /**
     * Validation group for loan-specific operations
     */
    public interface LoanOperation {}
    
    /**
     * Validation group for recommendation operations
     */
    public interface RecommendationOperation {}
}