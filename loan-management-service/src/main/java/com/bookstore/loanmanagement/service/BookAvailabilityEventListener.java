package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.event.BookAvailabilityChangedEvent;
import com.bookstore.loanmanagement.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Listener for book availability change events from the book catalog service
 * Handles synchronization between loan management and book catalog services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookAvailabilityEventListener {
    
    private final LoanRepository loanRepository;
    
    /**
     * Handle book availability changed events
     * This allows the loan service to stay synchronized with book catalog changes
     */
    @RabbitListener(queues = "loan.book.availability")
    public void handleBookAvailabilityChanged(BookAvailabilityChangedEvent event) {
        log.info("Received book availability changed event: bookId={}, previousAvailable={}, currentAvailable={}, reason={}, correlationId={}", 
                event.getBookId(), 
                event.getPreviousAvailableCopies(), 
                event.getCurrentAvailableCopies(),
                event.getChangeReason(),
                event.getCorrelationId());
        
        try {
            // Synchronize loan data with book availability
            synchronizeLoanData(event);
            
            // Log the availability change for monitoring and debugging
            if (event.getCurrentAvailableCopies() != null && event.getCurrentAvailableCopies() == 0) {
                log.warn("Book is now unavailable: bookId={}, title={}, correlationId={}", 
                        event.getBookId(), event.getTitle(), event.getCorrelationId());
            } else if (event.getPreviousAvailableCopies() != null && 
                       event.getPreviousAvailableCopies() == 0 && 
                       event.getCurrentAvailableCopies() != null && 
                       event.getCurrentAvailableCopies() > 0) {
                log.info("Book is now available again: bookId={}, title={}, availableCopies={}, correlationId={}", 
                        event.getBookId(), event.getTitle(), event.getCurrentAvailableCopies(), 
                        event.getCorrelationId());
            }
            
            // Future enhancement: Could trigger notifications to users waiting for this book
            // Future enhancement: Could update cached book availability data
            
        } catch (Exception e) {
            log.error("Error processing book availability changed event: bookId={}, error={}, correlationId={}", 
                    event.getBookId(), e.getMessage(), event.getCorrelationId(), e);
            // Don't throw exception - we don't want to requeue the message
        }
    }
    
    /**
     * Synchronize loan data based on book availability changes
     * This ensures consistency between services
     */
    private void synchronizeLoanData(BookAvailabilityChangedEvent event) {
        // Verify active loans match the book availability change
        List<Loan> activeLoans = loanRepository.findByBookIdAndStatus(event.getBookId(), LoanStatus.ACTIVE);
        
        log.debug("Synchronizing loan data: bookId={}, activeLoans={}, availableCopies={}, correlationId={}", 
                event.getBookId(), activeLoans.size(), event.getCurrentAvailableCopies(), 
                event.getCorrelationId());
        
        // Check for inconsistencies
        if ("BORROWED".equals(event.getChangeReason())) {
            // A book was borrowed - verify we have a corresponding active loan
            log.debug("Book borrowed event received, active loans count: {}", activeLoans.size());
        } else if ("RETURNED".equals(event.getChangeReason())) {
            // A book was returned - this should correspond to a loan return
            log.debug("Book returned event received, active loans count: {}", activeLoans.size());
        }
        
        // Log any potential inconsistencies for monitoring
        if (event.getTotalCopies() != null && event.getCurrentAvailableCopies() != null) {
            int expectedBorrowed = event.getTotalCopies() - event.getCurrentAvailableCopies();
            if (activeLoans.size() != expectedBorrowed) {
                log.warn("Potential inconsistency detected: bookId={}, activeLoans={}, expectedBorrowed={}, correlationId={}", 
                        event.getBookId(), activeLoans.size(), expectedBorrowed, event.getCorrelationId());
            }
        }
    }
}
