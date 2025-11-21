package com.bookstore.service;

import com.bookstore.entity.LoanStatus;
import com.bookstore.entity.LoanTracking;
import com.bookstore.repository.LoanTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for tracking loan events and history
 */
@Service
@Transactional
public class LoanTrackingService {
    
    private final LoanTrackingRepository loanTrackingRepository;
    
    @Autowired
    public LoanTrackingService(LoanTrackingRepository loanTrackingRepository) {
        this.loanTrackingRepository = loanTrackingRepository;
    }
    
    /**
     * Record a notification sent event
     */
    public void recordNotificationSent(Long loanId, String notificationType) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setEventType("NOTIFICATION_SENT");
        tracking.setEventDescription(notificationType);
        tracking.setEventTimestamp(LocalDateTime.now());
        
        loanTrackingRepository.save(tracking);
    }
    
    /**
     * Record a loan status change
     */
    public void recordStatusChange(Long loanId, LoanStatus fromStatus, LoanStatus toStatus) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setEventType("STATUS_CHANGE");
        tracking.setEventDescription(String.format("Status changed from %s to %s", fromStatus, toStatus));
        tracking.setEventTimestamp(LocalDateTime.now());
        
        loanTrackingRepository.save(tracking);
    }
    
    /**
     * Record a loan extension
     */
    public void recordLoanExtension(Long loanId, LocalDate oldDueDate, LocalDate newDueDate, int daysExtended) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setEventType("LOAN_EXTENDED");
        tracking.setEventDescription(String.format("Loan extended by %d days. Due date changed from %s to %s", 
            daysExtended, oldDueDate, newDueDate));
        tracking.setEventTimestamp(LocalDateTime.now());
        
        loanTrackingRepository.save(tracking);
    }
    
    /**
     * Record a loan creation
     */
    public void recordLoanCreated(Long loanId, String borrowerEmail, Long bookId) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setEventType("LOAN_CREATED");
        tracking.setEventDescription(String.format("Loan created for borrower %s, book ID %d", borrowerEmail, bookId));
        tracking.setEventTimestamp(LocalDateTime.now());
        
        loanTrackingRepository.save(tracking);
    }
    
    /**
     * Record a loan return
     */
    public void recordLoanReturned(Long loanId, boolean wasOverdue) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loanId);
        tracking.setEventType("LOAN_RETURNED");
        tracking.setEventDescription(wasOverdue ? "Book returned late" : "Book returned on time");
        tracking.setEventTimestamp(LocalDateTime.now());
        
        loanTrackingRepository.save(tracking);
    }
    
    /**
     * Check if a notification was sent today for a specific loan
     */
    @Transactional(readOnly = true)
    public boolean wasNotificationSentToday(Long loanId, String notificationType) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        return loanTrackingRepository.existsByLoanIdAndEventTypeAndEventDescriptionAndEventTimestampBetween(
            loanId, "NOTIFICATION_SENT", notificationType, startOfDay, endOfDay);
    }
    
    /**
     * Get tracking history for a specific loan
     */
    @Transactional(readOnly = true)
    public Page<LoanTracking> getLoanTrackingHistory(Long loanId, Pageable pageable) {
        return loanTrackingRepository.findByLoanIdOrderByEventTimestampDesc(loanId, pageable);
    }
    
    /**
     * Get all tracking events for a date range
     */
    @Transactional(readOnly = true)
    public Page<LoanTracking> getTrackingEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return loanTrackingRepository.findByEventTimestampBetweenOrderByEventTimestampDesc(startDate, endDate, pageable);
    }
    
    /**
     * Get tracking events by type
     */
    @Transactional(readOnly = true)
    public Page<LoanTracking> getTrackingEventsByType(String eventType, Pageable pageable) {
        return loanTrackingRepository.findByEventTypeOrderByEventTimestampDesc(eventType, pageable);
    }
    
    /**
     * Get notification statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStatistics() {
        List<Object[]> stats = loanTrackingRepository.getNotificationStatistics();
        
        Map<String, Long> result = new java.util.HashMap<>();
        for (Object[] stat : stats) {
            result.put((String) stat[0], (Long) stat[1]);
        }
        
        return result;
    }
    
    /**
     * Get tracking statistics for a specific loan
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getLoanTrackingStatistics(Long loanId) {
        List<Object[]> stats = loanTrackingRepository.getLoanTrackingStatistics(loanId);
        
        Map<String, Long> result = new java.util.HashMap<>();
        for (Object[] stat : stats) {
            result.put((String) stat[0], (Long) stat[1]);
        }
        
        return result;
    }
    
    /**
     * Get recent tracking events
     */
    @Transactional(readOnly = true)
    public List<LoanTracking> getRecentTrackingEvents(int limit) {
        return loanTrackingRepository.findRecentEvents(limit);
    }
    
    /**
     * Clean up old tracking records (older than specified days)
     */
    public int cleanupOldTrackingRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return loanTrackingRepository.deleteByEventTimestampBefore(cutoffDate);
    }
}