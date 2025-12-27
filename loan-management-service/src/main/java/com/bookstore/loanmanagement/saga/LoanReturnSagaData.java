package com.bookstore.loanmanagement.saga;

import com.bookstore.loanmanagement.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data holder for loan return saga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReturnSagaData {
    private String sagaId;
    private String correlationId;
    private Long loanId;
    private Long userId;
    private Long bookId;
    private LoanStatus originalStatus;
    private boolean wasOverdue;
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
