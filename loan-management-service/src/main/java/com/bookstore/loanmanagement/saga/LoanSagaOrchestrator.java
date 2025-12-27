package com.bookstore.loanmanagement.saga;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.event.LoanCreatedEvent;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.BookNotFoundException;
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
 * Orchestrates the loan creation saga with compensation logic
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoanSagaOrchestrator {
    
    private final LoanRepository loanRepository;
    private final BookCatalogClient bookCatalogClient;
    private final LoanTrackingService loanTrackingService;
    private final LoanEventPublisher loanEventPublisher;
    
    // In-memory saga state tracking (in production, use Redis or database)
    private final ConcurrentHashMap<String, LoanSagaData> sagaStates = new ConcurrentHashMap<>();
    
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    
    /**
     * Execute loan creation saga
     */
    public Loan executeLoanCreationSaga(LoanRequest request) {
        String sagaId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        
        LoanSagaData sagaData = LoanSagaData.builder()
            .sagaId(sagaId)
            .correlationId(correlationId)
            .userId(request.getUserId())
            .bookId(request.getBookId())
            .state(SagaState.STARTED)
            .startedAt(LocalDateTime.now())
            .retryCount(0)
            .build();
        
        sagaStates.put(sagaId, sagaData);
        
        try {
            log.info("Starting loan creation saga: sagaId={}, userId={}, bookId={}", 
                    sagaId, request.getUserId(), request.getBookId());
            
            // Step 1: Create loan record
            Loan loan = createLoanRecord(sagaData, request);
            sagaData.setLoanId(loan.getId());
            sagaData.setState(SagaState.LOAN_CREATED);
            
            // Step 2: Reserve book in catalog service
            reserveBook(sagaData);
            sagaData.setState(SagaState.BOOK_RESERVED);
            
            // Step 3: Record tracking and publish event
            completeSaga(sagaData, loan);
            sagaData.setState(SagaState.COMPLETED);
            sagaData.setCompletedAt(LocalDateTime.now());
            
            log.info("Loan creation saga completed successfully: sagaId={}, loanId={}", 
                    sagaId, loan.getId());
            
            return loan;
            
        } catch (Exception e) {
            log.error("Loan creation saga failed: sagaId={}, error={}", sagaId, e.getMessage());
            sagaData.setState(SagaState.FAILED);
            sagaData.setFailureReason(e.getMessage());
            
            // Attempt compensation
            compensate(sagaData);
            
            throw e;
        } finally {
            // Clean up saga state after some time (in production, use TTL)
            scheduleCleanup(sagaId);
        }
    }
    
    /**
     * Step 1: Create loan record in database
     */
    @Transactional
    protected Loan createLoanRecord(LoanSagaData sagaData, LoanRequest request) {
        log.info("Saga step 1: Creating loan record - sagaId={}", sagaData.getSagaId());
        
        Loan loan = new Loan();
        loan.setUserId(request.getUserId());
        loan.setBookId(request.getBookId());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        loan.setStatus(LoanStatus.PENDING); // Start as PENDING until saga completes
        
        return loanRepository.save(loan);
    }
    
    /**
     * Step 2: Reserve book in catalog service
     */
    protected void reserveBook(LoanSagaData sagaData) {
        log.info("Saga step 2: Reserving book - sagaId={}, bookId={}", 
                sagaData.getSagaId(), sagaData.getBookId());
        
        try {
            bookCatalogClient.borrowBook(sagaData.getBookId());
        } catch (FeignException.NotFound e) {
            throw new BookNotFoundException("Book not found with ID: " + sagaData.getBookId());
        } catch (FeignException e) {
            log.error("Error reserving book: {}", e.getMessage());
            throw new BookNotAvailableException("Failed to reserve book");
        }
    }
    
    /**
     * Step 3: Complete saga - update loan status and publish events
     */
    @Transactional
    protected void completeSaga(LoanSagaData sagaData, Loan loan) {
        log.info("Saga step 3: Completing saga - sagaId={}, loanId={}", 
                sagaData.getSagaId(), sagaData.getLoanId());
        
        // Update loan status to ACTIVE
        loan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loan);
        
        // Record tracking
        loanTrackingService.recordLoanCreated(loan.getId(), loan.getUserId(), loan.getBookId());
        
        // Publish loan created event
        LoanCreatedEvent event = new LoanCreatedEvent(
            loan.getId(),
            loan.getUserId(),
            loan.getBookId(),
            loan.getLoanDate(),
            loan.getDueDate(),
            loan.getStatus().name(),
            sagaData.getCorrelationId()
        );
        loanEventPublisher.publishLoanCreated(event);
    }
    
    /**
     * Compensate for failed saga
     */
    protected void compensate(LoanSagaData sagaData) {
        log.warn("Starting compensation for failed saga: sagaId={}, state={}", 
                sagaData.getSagaId(), sagaData.getState());
        
        sagaData.setState(SagaState.COMPENSATING);
        
        try {
            // Compensate based on how far the saga progressed
            switch (sagaData.getState()) {
                case BOOK_RESERVED:
                    // Book was reserved, need to unreserve it
                    compensateBookReservation(sagaData);
                    // Fall through to compensate loan creation
                case LOAN_CREATED:
                    // Loan was created, need to delete or mark as failed
                    compensateLoanCreation(sagaData);
                    break;
                default:
                    log.info("No compensation needed for state: {}", sagaData.getState());
            }
            
            sagaData.setState(SagaState.COMPENSATED);
            log.info("Compensation completed successfully: sagaId={}", sagaData.getSagaId());
            
        } catch (Exception e) {
            log.error("Compensation failed: sagaId={}, error={}", sagaData.getSagaId(), e.getMessage());
            throw new SagaCompensationException("Failed to compensate saga: " + e.getMessage());
        }
    }
    
    /**
     * Compensate book reservation
     */
    protected void compensateBookReservation(LoanSagaData sagaData) {
        log.info("Compensating book reservation: sagaId={}, bookId={}", 
                sagaData.getSagaId(), sagaData.getBookId());
        
        try {
            bookCatalogClient.returnBook(sagaData.getBookId());
        } catch (Exception e) {
            log.error("Failed to compensate book reservation: {}", e.getMessage());
            // Log but don't fail compensation - manual intervention may be needed
        }
    }
    
    /**
     * Compensate loan creation
     */
    @Transactional
    protected void compensateLoanCreation(LoanSagaData sagaData) {
        log.info("Compensating loan creation: sagaId={}, loanId={}", 
                sagaData.getSagaId(), sagaData.getLoanId());
        
        if (sagaData.getLoanId() != null) {
            loanRepository.findById(sagaData.getLoanId()).ifPresent(loan -> {
                loan.setStatus(LoanStatus.CANCELLED);
                loanRepository.save(loan);
                loanTrackingService.recordLoanCancelled(loan.getId(), sagaData.getFailureReason());
            });
        }
    }
    
    /**
     * Get saga state
     */
    public LoanSagaData getSagaState(String sagaId) {
        return sagaStates.get(sagaId);
    }
    
    /**
     * Schedule cleanup of saga state
     */
    private void scheduleCleanup(String sagaId) {
        // In production, use a scheduled task or TTL mechanism
        // For now, just log
        log.debug("Saga state cleanup scheduled: sagaId={}", sagaId);
    }
}
