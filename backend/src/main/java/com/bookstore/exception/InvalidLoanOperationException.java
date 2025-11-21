package com.bookstore.exception;

public class InvalidLoanOperationException extends RuntimeException {
    
    public InvalidLoanOperationException(String message) {
        super(message);
    }
    
    public InvalidLoanOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}