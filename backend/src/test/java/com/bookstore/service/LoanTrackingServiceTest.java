package com.bookstore.service;

import com.bookstore.entity.LoanStatus;
import com.bookstore.entity.LoanTracking;
import com.bookstore.repository.LoanTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanTrackingServiceTest {
    
    @Mock
    private LoanTrackingRepository loanTrackingRepository;
    
    @InjectMocks
    private LoanTrackingService loanTrackingService;
    
    private LoanTracking testTracking;
    
    @BeforeEach
    void setUp() {
        testTracking = new LoanTracking();
        testTracking.setId(1L);
        testTracking.setLoanId(1L);
        testTracking.setEventType("NOTIFICATION_SENT");
        testTracking.setEventDescription("LOAN_CONFIRMATION");
        testTracking.setEventTimestamp(LocalDateTime.now());
    }
    
    @Test
    void testRecordNotificationSent() {
        // Given
        Long loanId = 1L;
        String notificationType = "LOAN_CONFIRMATION";
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordNotificationSent(loanId, notificationType);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("NOTIFICATION_SENT", savedTracking.getEventType());
        assertEquals(notificationType, savedTracking.getEventDescription());
        assertNotNull(savedTracking.getEventTimestamp());
    }
    
    @Test
    void testRecordStatusChange() {
        // Given
        Long loanId = 1L;
        LoanStatus fromStatus = LoanStatus.ACTIVE;
        LoanStatus toStatus = LoanStatus.OVERDUE;
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordStatusChange(loanId, fromStatus, toStatus);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("STATUS_CHANGE", savedTracking.getEventType());
        assertTrue(savedTracking.getEventDescription().contains("Active"));
        assertTrue(savedTracking.getEventDescription().contains("Overdue"));
    }
    
    @Test
    void testRecordLoanExtension() {
        // Given
        Long loanId = 1L;
        LocalDate oldDueDate = LocalDate.now().plusDays(7);
        LocalDate newDueDate = LocalDate.now().plusDays(14);
        int daysExtended = 7;
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordLoanExtension(loanId, oldDueDate, newDueDate, daysExtended);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("LOAN_EXTENDED", savedTracking.getEventType());
        assertTrue(savedTracking.getEventDescription().contains("7 days"));
    }
    
    @Test
    void testRecordLoanCreated() {
        // Given
        Long loanId = 1L;
        String borrowerEmail = "test@email.com";
        Long bookId = 1L;
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordLoanCreated(loanId, borrowerEmail, bookId);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("LOAN_CREATED", savedTracking.getEventType());
        assertTrue(savedTracking.getEventDescription().contains(borrowerEmail));
        assertTrue(savedTracking.getEventDescription().contains(bookId.toString()));
    }
    
    @Test
    void testRecordLoanReturned() {
        // Given
        Long loanId = 1L;
        boolean wasOverdue = true;
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordLoanReturned(loanId, wasOverdue);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("LOAN_RETURNED", savedTracking.getEventType());
        assertTrue(savedTracking.getEventDescription().contains("late"));
    }
    
    @Test
    void testRecordLoanReturned_OnTime() {
        // Given
        Long loanId = 1L;
        boolean wasOverdue = false;
        
        when(loanTrackingRepository.save(any(LoanTracking.class))).thenReturn(testTracking);
        
        // When
        loanTrackingService.recordLoanReturned(loanId, wasOverdue);
        
        // Then
        ArgumentCaptor<LoanTracking> trackingCaptor = ArgumentCaptor.forClass(LoanTracking.class);
        verify(loanTrackingRepository).save(trackingCaptor.capture());
        
        LoanTracking savedTracking = trackingCaptor.getValue();
        assertEquals(loanId, savedTracking.getLoanId());
        assertEquals("LOAN_RETURNED", savedTracking.getEventType());
        assertTrue(savedTracking.getEventDescription().contains("on time"));
    }
    
    @Test
    void testWasNotificationSentToday_True() {
        // Given
        Long loanId = 1L;
        String notificationType = "DUE_REMINDER";
        
        when(loanTrackingRepository.existsByLoanIdAndEventTypeAndEventDescriptionAndEventTimestampBetween(
            eq(loanId), eq("NOTIFICATION_SENT"), eq(notificationType), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(true);
        
        // When
        boolean result = loanTrackingService.wasNotificationSentToday(loanId, notificationType);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testWasNotificationSentToday_False() {
        // Given
        Long loanId = 1L;
        String notificationType = "DUE_REMINDER";
        
        when(loanTrackingRepository.existsByLoanIdAndEventTypeAndEventDescriptionAndEventTimestampBetween(
            eq(loanId), eq("NOTIFICATION_SENT"), eq(notificationType), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(false);
        
        // When
        boolean result = loanTrackingService.wasNotificationSentToday(loanId, notificationType);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testGetLoanTrackingHistory() {
        // Given
        Long loanId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingRepository.findByLoanIdOrderByEventTimestampDesc(loanId, pageable))
            .thenReturn(trackingPage);
        
        // When
        Page<LoanTracking> result = loanTrackingService.getLoanTrackingHistory(loanId, pageable);
        
        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(testTracking, result.getContent().get(0));
    }
    
    @Test
    void testGetTrackingEventsByDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingRepository.findByEventTimestampBetweenOrderByEventTimestampDesc(startDate, endDate, pageable))
            .thenReturn(trackingPage);
        
        // When
        Page<LoanTracking> result = loanTrackingService.getTrackingEventsByDateRange(startDate, endDate, pageable);
        
        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(testTracking, result.getContent().get(0));
    }
    
    @Test
    void testGetTrackingEventsByType() {
        // Given
        String eventType = "NOTIFICATION_SENT";
        Pageable pageable = PageRequest.of(0, 10);
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingRepository.findByEventTypeOrderByEventTimestampDesc(eventType, pageable))
            .thenReturn(trackingPage);
        
        // When
        Page<LoanTracking> result = loanTrackingService.getTrackingEventsByType(eventType, pageable);
        
        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(testTracking, result.getContent().get(0));
    }
    
    @Test
    void testGetNotificationStatistics() {
        // Given
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{"LOAN_CONFIRMATION", 10L},
            new Object[]{"DUE_REMINDER", 5L},
            new Object[]{"OVERDUE_NOTIFICATION", 3L}
        );
        
        when(loanTrackingRepository.getNotificationStatistics()).thenReturn(mockStats);
        
        // When
        Map<String, Long> result = loanTrackingService.getNotificationStatistics();
        
        // Then
        assertEquals(3, result.size());
        assertEquals(10L, result.get("LOAN_CONFIRMATION"));
        assertEquals(5L, result.get("DUE_REMINDER"));
        assertEquals(3L, result.get("OVERDUE_NOTIFICATION"));
    }
    
    @Test
    void testGetLoanTrackingStatistics() {
        // Given
        Long loanId = 1L;
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{"NOTIFICATION_SENT", 3L},
            new Object[]{"STATUS_CHANGE", 1L},
            new Object[]{"LOAN_CREATED", 1L}
        );
        
        when(loanTrackingRepository.getLoanTrackingStatistics(loanId)).thenReturn(mockStats);
        
        // When
        Map<String, Long> result = loanTrackingService.getLoanTrackingStatistics(loanId);
        
        // Then
        assertEquals(3, result.size());
        assertEquals(3L, result.get("NOTIFICATION_SENT"));
        assertEquals(1L, result.get("STATUS_CHANGE"));
        assertEquals(1L, result.get("LOAN_CREATED"));
    }
    
    @Test
    void testGetRecentTrackingEvents() {
        // Given
        int limit = 10;
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        
        when(loanTrackingRepository.findRecentEvents(limit)).thenReturn(trackingList);
        
        // When
        List<LoanTracking> result = loanTrackingService.getRecentTrackingEvents(limit);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testTracking, result.get(0));
    }
    
    @Test
    void testCleanupOldTrackingRecords() {
        // Given
        int daysToKeep = 365;
        int expectedDeletedRecords = 50;
        
        when(loanTrackingRepository.deleteByEventTimestampBefore(any(LocalDateTime.class)))
            .thenReturn(expectedDeletedRecords);
        
        // When
        int result = loanTrackingService.cleanupOldTrackingRecords(daysToKeep);
        
        // Then
        assertEquals(expectedDeletedRecords, result);
        verify(loanTrackingRepository).deleteByEventTimestampBefore(any(LocalDateTime.class));
    }
}