package com.bookstore.exception;

/**
 * Exception thrown when attempting to create a resource that already exists
 */
public class DuplicateResourceException extends RuntimeException {
    
    private final String resourceType;
    private final String identifier;
    
    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.identifier = null;
    }
    
    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists with identifier: %s", resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.identifier = null;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getIdentifier() {
        return identifier;
    }
}