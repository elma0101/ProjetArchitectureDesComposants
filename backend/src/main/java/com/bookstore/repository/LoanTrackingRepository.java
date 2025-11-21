package com.bookstore.repository;

import com.bookstore.entity.LoanTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LoanTracking entity
 */
@Repository
public interface LoanTrackingRepository extends JpaRepository<LoanTracking, Long> {
    
    /**
     * Find tracking events by loan ID
     */
    Page<LoanTracking> findByLoanIdOrderByEventTimestampDesc(Long loanId, Pageable pageable);
    
    /**
     * Find tracking events by event type
     */
    Page<LoanTracking> findByEventTypeOrderByEventTimestampDesc(String eventType, Pageable pageable);
    
    /**
     * Find tracking events by date range
     */
    Page<LoanTracking> findByEventTimestampBetweenOrderByEventTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Check if a specific notification was sent for a loan within a time range
     */
    boolean existsByLoanIdAndEventTypeAndEventDescriptionAndEventTimestampBetween(
        Long loanId, String eventType, String eventDescription, 
        LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get notification statistics
     */
    @Query("SELECT lt.eventDescription, COUNT(lt) FROM LoanTracking lt " +
           "WHERE lt.eventType = 'NOTIFICATION_SENT' " +
           "GROUP BY lt.eventDescription")
    List<Object[]> getNotificationStatistics();
    
    /**
     * Get tracking statistics for a specific loan
     */
    @Query("SELECT lt.eventType, COUNT(lt) FROM LoanTracking lt " +
           "WHERE lt.loanId = :loanId " +
           "GROUP BY lt.eventType")
    List<Object[]> getLoanTrackingStatistics(@Param("loanId") Long loanId);
    
    /**
     * Get recent tracking events
     */
    @Query("SELECT lt FROM LoanTracking lt ORDER BY lt.eventTimestamp DESC")
    List<LoanTracking> findRecentEvents(int limit);
    
    /**
     * Delete old tracking records
     */
    @Modifying
    @Query("DELETE FROM LoanTracking lt WHERE lt.eventTimestamp < :cutoffDate")
    int deleteByEventTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count tracking events by loan ID and event type
     */
    Long countByLoanIdAndEventType(Long loanId, String eventType);
    
    /**
     * Find tracking events by loan ID and event type
     */
    List<LoanTracking> findByLoanIdAndEventTypeOrderByEventTimestampDesc(Long loanId, String eventType);
    
    /**
     * Find tracking events for multiple loan IDs
     */
    @Query("SELECT lt FROM LoanTracking lt WHERE lt.loanId IN :loanIds ORDER BY lt.eventTimestamp DESC")
    Page<LoanTracking> findByLoanIdInOrderByEventTimestampDesc(@Param("loanIds") List<Long> loanIds, Pageable pageable);
    
    /**
     * Get daily notification counts for the last N days
     */
    @Query("SELECT DATE(lt.eventTimestamp), COUNT(lt) FROM LoanTracking lt " +
           "WHERE lt.eventType = 'NOTIFICATION_SENT' " +
           "AND lt.eventTimestamp >= :startDate " +
           "GROUP BY DATE(lt.eventTimestamp) " +
           "ORDER BY DATE(lt.eventTimestamp)")
    List<Object[]> getDailyNotificationCounts(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get event type statistics for a date range
     */
    @Query("SELECT lt.eventType, COUNT(lt) FROM LoanTracking lt " +
           "WHERE lt.eventTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY lt.eventType")
    List<Object[]> getEventTypeStatistics(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}