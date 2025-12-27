package com.bookstore.loanmanagement.saga;

/**
 * Represents the state of a Saga transaction
 */
public enum SagaState {
    STARTED,
    LOAN_CREATED,
    BOOK_RESERVED,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
