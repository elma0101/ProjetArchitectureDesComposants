package com.bookstore.exception;

public class BookNotAvailableException extends RuntimeException {
    
    public BookNotAvailableException(String message) {
        super(message);
    }
    
    public BookNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}