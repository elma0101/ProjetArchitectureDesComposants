package com.bookstore.loanmanagement.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Event published when a loan is created
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoanCreatedEvent extends LoanEvent {
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String status;
    
    public LoanCreatedEvent(Long loanId, Long userId, Long bookId, 
                           LocalDate loanDate, LocalDate dueDate, 
                           String status, String correlationId) {
        super("LOAN_CREATED", loanId, userId, bookId, correlationId);
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = status;
    }
}
