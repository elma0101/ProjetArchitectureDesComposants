package com.bookstore.exception;

/**
 * Exception thrown when an author is not found
 */
public class AuthorNotFoundException extends RuntimeException {
    
    public AuthorNotFoundException(String message) {
        super(message);
    }
    
    public AuthorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AuthorNotFoundException(Long authorId) {
        super("Author not found with id: " + authorId);
    }
}