package com.bookstore.loanmanagement.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Event published when a loan is returned
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoanReturnedEvent extends LoanEvent {
    private LocalDate returnDate;
    private boolean wasOverdue;
    
    public LoanReturnedEvent(Long loanId, Long userId, Long bookId, 
                            LocalDate returnDate, boolean wasOverdue, 
                            String correlationId) {
        super("LOAN_RETURNED", loanId, userId, bookId, correlationId);
        this.returnDate = returnDate;
        this.wasOverdue = wasOverdue;
    }
}
