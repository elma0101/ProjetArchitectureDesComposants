package com.bookstore.loanmanagement.saga;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.event.LoanReturnedEvent;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.LoanNotFoundException;
import com.bookstore.loanmanagement.exception.SagaCompensationException;
import com.bookstore.loanmanagement.repository.LoanRepository;
import com.bookstore.loanmanagement.service.LoanEventPublisher;
import com.bookstore.loanmanagement.service.LoanTrackingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the loan return saga with compensation logic
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoanReturnSagaOrchestrator {
    
    private final LoanRepository loanRepository;
    private final BookCatalogClient bookCatalogClient;
    private final LoanTrackingService loanTrackingService;
    private final LoanEventPublisher loanEventPublisher;
    
    // In-memory saga state tracking
    private final ConcurrentHashMap<String, LoanReturnSagaData> sagaStates = new ConcurrentHashMap<>();
    
    /**
     * Execute loan return saga
     */
    public Loan executeLoanReturnSaga(Long loanId, String notes) {
        String sagaId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        
        // Load loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        LoanReturnSagaData sagaData = LoanReturnSagaData.builder()
            .sagaId(sagaId)
            .correlationId(correlationId)
            .loanId(loanId)
            .userId(loan.getUserId())
            .bookId(loan.getBookId())
            .originalStatus(loan.getStatus())
            .state(SagaState.STARTED)
            .startedAt(LocalDateTime.now())
            .retryCount(0)
            .build();
        
        sagaStates.put(sagaId, sagaData);
        
        try {
            log.info("Starting loan return saga: sagaId={}, loanId={}", sagaId, loanId);
            
            // Step 1: Update loan status to returned
            updateLoanToReturned(sagaData, loan);
            sagaData.setState(SagaState.LOAN_CREATED); // Reusing state enum
            
            // Step 2: Return book to catalog
            returnBookToCatalog(sagaData);
            sagaData.setState(SagaState.BOOK_RESERVED); // Reusing state enum
            
            // Step 3: Complete saga - record tracking and publish event
            completeReturnSaga(sagaData, loan);
            sagaData.setState(SagaState.COMPLETED);
            sagaData.setCompletedAt(LocalDateTime.now());
            
            log.info("Loan return saga completed successfully: sagaId={}, loanId={}", sagaId, loanId);
            
            return loan;
            
        } catch (Exception e) {
            log.error("Loan return saga failed: sagaId={}, error={}", sagaId, e.getMessage());
            sagaData.setState(SagaState.FAILED);
            sagaData.setFailureReason(e.getMessage());
            
            // Attempt compensation
            compensateReturn(sagaData, loan);
            
            throw e;
        } finally {
            scheduleCleanup(sagaId);
        }
    }
    
    /**
     * Step 1: Update loan status to returned
     */
    @Transactional
    protected void updateLoanToReturned(LoanReturnSagaData sagaData, Loan loan) {
        log.info("Saga step 1: Updating loan to returned - sagaId={}, loanId={}", 
                sagaData.getSagaId(), sagaData.getLoanId());
        
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        sagaData.setWasOverdue(loan.getReturnDate().isAfter(loan.getDueDate()));
        
        loanRepository.save(loan);
    }
    
    /**
     * Step 2: Return book to catalog service
     */
    protected void returnBookToCatalog(LoanReturnSagaData sagaData) {
        log.info("Saga step 2: Returning book to catalog - sagaId={}, bookId={}", 
                sagaData.getSagaId(), sagaData.getBookId());
        
        try {
            bookCatalogClient.returnBook(sagaData.getBookId());
        } catch (FeignException e) {
            log.error("Error returning book to catalog: {}", e.getMessage());
            throw new BookNotAvailableException("Failed to return book to catalog");
        }
    }
    
    /**
     * Step 3: Complete saga - record tracking and publish event
     */
    @Transactional
    protected void completeReturnSaga(LoanReturnSagaData sagaData, Loan loan) {
        log.info("Saga step 3: Completing return saga - sagaId={}, loanId={}", 
                sagaData.getSagaId(), sagaData.getLoanId());
        
        // Record tracking
        loanTrackingService.recordLoanReturned(loan.getId(), sagaData.isWasOverdue());
        
        // Publish loan returned event
        LoanReturnedEvent event = new LoanReturnedEvent(
            loan.getId(),
            loan.getUserId(),
            loan.getBookId(),
            loan.getReturnDate(),
            sagaData.isWasOverdue(),
            sagaData.getCorrelationId()
        );
        loanEventPublisher.publishLoanReturned(event);
    }
    
    /**
     * Compensate for failed return saga
     */
    protected void compensateReturn(LoanReturnSagaData sagaData, Loan loan) {
        log.warn("Starting compensation for failed return saga: sagaId={}, state={}", 
                sagaData.getSagaId(), sagaData.getState());
        
        sagaData.setState(SagaState.COMPENSATING);
        
        try {
            // Compensate based on how far the saga progressed
            switch (sagaData.getState()) {
                case BOOK_RESERVED:
                    // Book was returned to catalog, need to borrow it again
                    compensateBookReturn(sagaData);
                    // Fall through to compensate loan update
                case LOAN_CREATED:
                    // Loan was marked as returned, need to revert
                    compensateLoanReturn(sagaData, loan);
                    break;
                default:
                    log.info("No compensation needed for state: {}", sagaData.getState());
            }
            
            sagaData.setState(SagaState.COMPENSATED);
            log.info("Compensation completed successfully: sagaId={}", sagaData.getSagaId());
            
        } catch (Exception e) {
            log.error("Compensation failed: sagaId={}, error={}", sagaData.getSagaId(), e.getMessage());
            throw new SagaCompensationException("Failed to compensate return saga: " + e.getMessage());
        }
    }
    
    /**
     * Compensate book return
     */
    protected void compensateBookReturn(LoanReturnSagaData sagaData) {
        log.info("Compensating book return: sagaId={}, bookId={}", 
                sagaData.getSagaId(), sagaData.getBookId());
        
        try {
            bookCatalogClient.borrowBook(sagaData.getBookId());
        } catch (Exception e) {
            log.error("Failed to compensate book return: {}", e.getMessage());
            // Log but don't fail compensation - manual intervention may be needed
        }
    }
    
    /**
     * Compensate loan return
     */
    @Transactional
    protected void compensateLoanReturn(LoanReturnSagaData sagaData, Loan loan) {
        log.info("Compensating loan return: sagaId={}, loanId={}", 
                sagaData.getSagaId(), sagaData.getLoanId());
        
        loan.setReturnDate(null);
        loan.setStatus(sagaData.getOriginalStatus());
        loanRepository.save(loan);
        
        loanTrackingService.recordLoanCancelled(loan.getId(), 
                "Return cancelled due to saga failure: " + sagaData.getFailureReason());
    }
    
    /**
     * Get saga state
     */
    public LoanReturnSagaData getSagaState(String sagaId) {
        return sagaStates.get(sagaId);
    }
    
    /**
     * Schedule cleanup of saga state
     */
    private void scheduleCleanup(String sagaId) {
        log.debug("Saga state cleanup scheduled: sagaId={}", sagaId);
    }
}
