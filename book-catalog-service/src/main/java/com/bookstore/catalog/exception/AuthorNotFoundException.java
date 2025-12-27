package com.bookstore.catalog.exception;

/**
 * Exception thrown when an author is not found
 */
public class AuthorNotFoundException extends RuntimeException {
    
    public AuthorNotFoundException(String message) {
        super(message);
    }
    
    public AuthorNotFoundException(Long id) {
        super("Author not found with id: " + id);
    }
    
    public AuthorNotFoundException(String field, String value) {
        super("Author not found with " + field + ": " + value);
    }
}
