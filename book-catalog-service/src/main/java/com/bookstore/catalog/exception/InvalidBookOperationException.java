package com.bookstore.catalog.exception;

/**
 * Exception thrown when an invalid book operation is attempted
 */
public class InvalidBookOperationException extends RuntimeException {
    
    public InvalidBookOperationException(String message) {
        super(message);
    }
    
    public InvalidBookOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
