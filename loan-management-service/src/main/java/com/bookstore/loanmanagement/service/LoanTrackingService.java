package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.entity.LoanTracking;
import com.bookstore.loanmanagement.repository.LoanTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoanTrackingService {
    
    private final LoanTrackingRepository loanTrackingRepository;
    
    /**
     * Record loan creation
     */
    public void recordLoanCreated(Long loanId, Long userId, Long bookId) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setStatus(LoanStatus.ACTIVE);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setNotes(String.format("Loan created for user %d, book %d", userId, bookId));
        tracking.setChangedBy("SYSTEM");
        
        loanTrackingRepository.save(tracking);
        log.info("Recorded loan creation: loanId={}, userId={}, bookId={}", loanId, userId, bookId);
    }
    
    /**
     * Record loan return
     */
    public void recordLoanReturned(Long loanId, boolean wasOverdue) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setStatus(LoanStatus.RETURNED);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setNotes(wasOverdue ? "Loan returned (was overdue)" : "Loan returned on time");
        tracking.setChangedBy("SYSTEM");
        
        loanTrackingRepository.save(tracking);
        log.info("Recorded loan return: loanId={}, wasOverdue={}", loanId, wasOverdue);
    }
    
    /**
     * Record loan extension
     */
    public void recordLoanExtension(Long loanId, LocalDate oldDueDate, LocalDate newDueDate, int additionalDays) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setStatus(LoanStatus.ACTIVE);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setNotes(String.format("Loan extended by %d days. Old due date: %s, New due date: %s", 
                                       additionalDays, oldDueDate, newDueDate));
        tracking.setChangedBy("SYSTEM");
        
        loanTrackingRepository.save(tracking);
        log.info("Recorded loan extension: loanId={}, additionalDays={}", loanId, additionalDays);
    }
    
    /**
     * Record loan marked as overdue
     */
    public void recordLoanOverdue(Long loanId) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setStatus(LoanStatus.OVERDUE);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setNotes("Loan marked as overdue");
        tracking.setChangedBy("SYSTEM");
        
        loanTrackingRepository.save(tracking);
        log.info("Recorded loan overdue: loanId={}", loanId);
    }
    
    /**
     * Record loan cancellation (for saga compensation)
     */
    public void recordLoanCancelled(Long loanId, String reason) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setStatus(LoanStatus.CANCELLED);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setNotes("Loan cancelled: " + reason);
        tracking.setChangedBy("SYSTEM");
        
        loanTrackingRepository.save(tracking);
        log.info("Recorded loan cancellation: loanId={}, reason={}", loanId, reason);
    }
    
    /**
     * Get loan history
     */
    @Transactional(readOnly = true)
    public List<LoanTracking> getLoanHistory(Long loanId) {
        return loanTrackingRepository.findByLoanIdOrderByTimestampDesc(loanId);
    }
}
