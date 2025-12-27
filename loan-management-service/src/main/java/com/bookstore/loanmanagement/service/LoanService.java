package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.dto.BookResponse;
import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.dto.LoanResponse;
import com.bookstore.loanmanagement.dto.LoanStatistics;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.BookNotFoundException;
import com.bookstore.loanmanagement.exception.InvalidLoanOperationException;
import com.bookstore.loanmanagement.exception.LoanNotFoundException;
import com.bookstore.loanmanagement.repository.LoanRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    
    private final LoanRepository loanRepository;
    private final BookCatalogClient bookCatalogClient;
    private final LoanTrackingService loanTrackingService;
    private final com.bookstore.loanmanagement.saga.LoanSagaOrchestrator sagaOrchestrator;
    private final LoanEventPublisher loanEventPublisher;
    
    // Default loan period in days
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final int MAX_EXTENSION_DAYS = 30;
    
    /**
     * Create a new loan for a book using Saga pattern
     */
    public LoanResponse createLoan(LoanRequest request) {
        log.info("Creating loan for userId={}, bookId={}", request.getUserId(), request.getBookId());
        
        // Check book availability via Book Catalog Service
        BookResponse book;
        try {
            book = bookCatalogClient.checkAvailability(request.getBookId());
        } catch (FeignException.NotFound e) {
            throw new BookNotFoundException("Book not found with ID: " + request.getBookId());
        } catch (FeignException e) {
            log.error("Error checking book availability: {}", e.getMessage());
            throw new BookNotAvailableException("Unable to verify book availability");
        }
        
        // Validate book availability
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("Book is not available for loan: " + book.getTitle());
        }
        
        // Check if user already has an active loan for this book
        long activeLoansForBook = loanRepository.findByUserIdAndStatus(request.getUserId(), LoanStatus.ACTIVE)
                .stream()
                .filter(loan -> loan.getBookId().equals(request.getBookId()))
                .count();
        
        if (activeLoansForBook > 0) {
            throw new InvalidLoanOperationException("User already has an active loan for this book");
        }
        
        // Execute loan creation saga with distributed transaction handling
        Loan loan = sagaOrchestrator.executeLoanCreationSaga(request);
        
        log.info("Loan created successfully via saga: loanId={}", loan.getId());
        return mapToResponse(loan);
    }
    
    /**
     * Return a borrowed book with distributed transaction handling
     */
    public LoanResponse returnLoan(Long loanId, String notes) {
        log.info("Processing return for loanId={}", loanId);
        
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidLoanOperationException("Loan is not active and cannot be returned");
        }
        
        String correlationId = java.util.UUID.randomUUID().toString();
        
        try {
            // Step 1: Mark loan as returned
            loan.setReturnDate(LocalDate.now());
            loan.setStatus(LoanStatus.RETURNED);
            boolean wasOverdue = loan.getReturnDate().isAfter(loan.getDueDate());
            
            Loan savedLoan = loanRepository.save(loan);
            
            // Step 2: Update book availability in Book Catalog Service
            try {
                bookCatalogClient.returnBook(loan.getBookId());
            } catch (FeignException e) {
                log.error("Error updating book availability on return: {}", e.getMessage());
                // Compensate: revert loan status
                loan.setReturnDate(null);
                loan.setStatus(wasOverdue ? LoanStatus.OVERDUE : LoanStatus.ACTIVE);
                loanRepository.save(loan);
                throw new BookNotAvailableException("Failed to update book availability, loan return cancelled");
            }
            
            // Step 3: Record loan return and publish event
            loanTrackingService.recordLoanReturned(savedLoan.getId(), wasOverdue);
            
            // Publish loan returned event
            com.bookstore.loanmanagement.event.LoanReturnedEvent event = 
                new com.bookstore.loanmanagement.event.LoanReturnedEvent(
                    savedLoan.getId(),
                    savedLoan.getUserId(),
                    savedLoan.getBookId(),
                    savedLoan.getReturnDate(),
                    wasOverdue,
                    correlationId
                );
            loanEventPublisher.publishLoanReturned(event);
            
            log.info("Loan returned successfully: loanId={}, wasOverdue={}, correlationId={}", 
                    savedLoan.getId(), wasOverdue, correlationId);
            return mapToResponse(savedLoan);
            
        } catch (Exception e) {
            log.error("Error during loan return: loanId={}, error={}", loanId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extend loan due date
     */
    public LoanResponse extendLoan(Long loanId, int additionalDays) {
        log.info("Extending loan: loanId={}, additionalDays={}", loanId, additionalDays);
        
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidLoanOperationException("Only active loans can be extended");
        }
        
        if (additionalDays <= 0 || additionalDays > MAX_EXTENSION_DAYS) {
            throw new InvalidLoanOperationException("Extension days must be between 1 and " + MAX_EXTENSION_DAYS);
        }
        
        LocalDate oldDueDate = loan.getDueDate();
        loan.setDueDate(loan.getDueDate().plusDays(additionalDays));
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Record loan extension
        loanTrackingService.recordLoanExtension(savedLoan.getId(), oldDueDate, savedLoan.getDueDate(), additionalDays);
        
        log.info("Loan extended successfully: loanId={}", savedLoan.getId());
        return mapToResponse(savedLoan);
    }
    
    /**
     * Get loan by ID
     */
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        return mapToResponse(loan);
    }
    
    /**
     * Get all loans for a user
     */
    @Transactional(readOnly = true)
    public Page<LoanResponse> getLoansByUserId(Long userId, Pageable pageable) {
        Page<Loan> loans = loanRepository.findByUserId(userId).stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                ));
        return loans.map(this::mapToResponse);
    }
    
    /**
     * Get active loans for a user
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getActiveLoansForUser(Long userId) {
        List<Loan> loans = loanRepository.findByUserIdAndStatus(userId, LoanStatus.ACTIVE);
        return loans.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Get all loans for a book
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByBookId(Long bookId) {
        List<Loan> loans = loanRepository.findByBookId(bookId);
        return loans.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Get overdue loans
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> getOverdueLoans() {
        List<Loan> loans = loanRepository.findOverdueLoans(LocalDate.now());
        return loans.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Search loans with multiple filters
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> searchLoans(Long userId, Long bookId, LoanStatus status, 
                                          LocalDate fromDate, LocalDate toDate, Boolean overdue) {
        log.info("Searching loans with filters");
        
        List<Loan> loans = loanRepository.findAll();
        
        // Apply filters
        if (userId != null) {
            loans = loans.stream()
                    .filter(loan -> loan.getUserId().equals(userId))
                    .toList();
        }
        
        if (bookId != null) {
            loans = loans.stream()
                    .filter(loan -> loan.getBookId().equals(bookId))
                    .toList();
        }
        
        if (status != null) {
            loans = loans.stream()
                    .filter(loan -> loan.getStatus() == status)
                    .toList();
        }
        
        if (fromDate != null) {
            loans = loans.stream()
                    .filter(loan -> !loan.getLoanDate().isBefore(fromDate))
                    .toList();
        }
        
        if (toDate != null) {
            loans = loans.stream()
                    .filter(loan -> !loan.getLoanDate().isAfter(toDate))
                    .toList();
        }
        
        if (overdue != null && overdue) {
            loans = loans.stream()
                    .filter(Loan::isOverdue)
                    .toList();
        }
        
        return loans.stream().map(this::mapToResponse).toList();
    }
    
    /**
     * Update overdue loans status
     * This method should be called periodically (e.g., daily) to update loan statuses
     */
    public int updateOverdueLoans() {
        log.info("Updating overdue loans");
        
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        int updatedCount = 0;
        
        for (Loan loan : activeLoans) {
            if (loan.isOverdue() && loan.getStatus() == LoanStatus.ACTIVE) {
                loan.setStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);
                loanTrackingService.recordLoanOverdue(loan.getId());
                updatedCount++;
            }
        }
        
        log.info("Updated {} loans to overdue status", updatedCount);
        return updatedCount;
    }
    
    /**
     * Get loan statistics
     */
    @Transactional(readOnly = true)
    public LoanStatistics getLoanStatistics() {
        long activeCount = loanRepository.findByStatus(LoanStatus.ACTIVE).size();
        long overdueCount = loanRepository.findOverdueLoans(LocalDate.now()).size();
        long returnedCount = loanRepository.findByStatus(LoanStatus.RETURNED).size();
        
        return new LoanStatistics(activeCount, overdueCount, returnedCount);
    }
    
    /**
     * Get loan history for a specific loan
     */
    @Transactional(readOnly = true)
    public List<com.bookstore.loanmanagement.entity.LoanTracking> getLoanHistory(Long loanId) {
        // Verify loan exists
        loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        return loanTrackingService.getLoanHistory(loanId);
    }
    
    /**
     * Get analytics: count of active loans for a user
     */
    @Transactional(readOnly = true)
    public long getActiveLoanCountForUser(Long userId) {
        return loanRepository.countActiveLoansForUser(userId);
    }
    
    /**
     * Get analytics: count of active loans for a book
     */
    @Transactional(readOnly = true)
    public long getActiveLoanCountForBook(Long bookId) {
        return loanRepository.countActiveLoansForBook(bookId);
    }
    
    /**
     * Map Loan entity to LoanResponse DTO
     */
    private LoanResponse mapToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setUserId(loan.getUserId());
        response.setBookId(loan.getBookId());
        response.setLoanDate(loan.getLoanDate());
        response.setDueDate(loan.getDueDate());
        response.setReturnDate(loan.getReturnDate());
        response.setStatus(loan.getStatus());
        response.setCreatedAt(loan.getCreatedAt());
        response.setUpdatedAt(loan.getUpdatedAt());
        response.setOverdue(loan.isOverdue());
        return response;
    }
}
