package com.bookstore.loanmanagement.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data holder for loan creation saga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSagaData {
    private String sagaId;
    private String correlationId;
    private Long userId;
    private Long bookId;
    private Long loanId;
    private SagaState state;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private int retryCount;
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public boolean canRetry() {
        return retryCount < 3;
    }
}
