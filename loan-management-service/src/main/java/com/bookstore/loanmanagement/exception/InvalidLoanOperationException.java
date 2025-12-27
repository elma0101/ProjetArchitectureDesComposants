package com.bookstore.loanmanagement.exception;

public class InvalidLoanOperationException extends RuntimeException {
    public InvalidLoanOperationException(String message) {
        super(message);
    }
}
