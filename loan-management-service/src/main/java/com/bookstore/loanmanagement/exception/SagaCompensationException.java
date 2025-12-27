package com.bookstore.loanmanagement.exception;

/**
 * Exception thrown when saga compensation fails
 */
public class SagaCompensationException extends RuntimeException {
    public SagaCompensationException(String message) {
        super(message);
    }
    
    public SagaCompensationException(String message, Throwable cause) {
        super(message, cause);
    }
}
