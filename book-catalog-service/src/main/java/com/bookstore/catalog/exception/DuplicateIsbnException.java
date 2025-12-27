package com.bookstore.catalog.exception;

/**
 * Exception thrown when attempting to create a book with a duplicate ISBN
 */
public class DuplicateIsbnException extends RuntimeException {
    
    public DuplicateIsbnException(String isbn) {
        super("Book with ISBN " + isbn + " already exists");
    }
    
    public DuplicateIsbnException(String message, Throwable cause) {
        super(message, cause);
    }
}
