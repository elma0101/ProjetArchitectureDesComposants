package com.bookstore.loanmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base class for loan-related events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class LoanEvent {
    private String eventId;
    private String correlationId;
    private LocalDateTime timestamp;
    private String eventType;
    private Long loanId;
    private Long userId;
    private Long bookId;
    
    public LoanEvent(String eventType, Long loanId, Long userId, Long bookId, String correlationId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
    }
}
