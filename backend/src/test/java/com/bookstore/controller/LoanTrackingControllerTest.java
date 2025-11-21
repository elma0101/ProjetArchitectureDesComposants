package com.bookstore.controller;

import com.bookstore.entity.LoanTracking;
import com.bookstore.service.LoanAnalyticsService;
import com.bookstore.service.LoanNotificationService;
import com.bookstore.service.LoanTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanTrackingController.class)
@Import(com.bookstore.config.TestSecurityConfig.class)
class LoanTrackingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private LoanTrackingService loanTrackingService;
    
    @MockBean
    private LoanAnalyticsService loanAnalyticsService;
    
    @MockBean
    private LoanNotificationService loanNotificationService;
    
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
    void testGetLoanTrackingHistory() throws Exception {
        // Given
        Long loanId = 1L;
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingService.getLoanTrackingHistory(eq(loanId), any()))
            .thenReturn(trackingPage);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/loan/{loanId}/history", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].loanId").value(1))
                .andExpect(jsonPath("$.content[0].eventType").value("NOTIFICATION_SENT"));
    }
    
    @Test
    void testGetTrackingEventsByType() throws Exception {
        // Given
        String eventType = "NOTIFICATION_SENT";
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingService.getTrackingEventsByType(eq(eventType), any()))
            .thenReturn(trackingPage);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/events/{eventType}", eventType)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].eventType").value(eventType));
    }
    
    @Test
    void testGetTrackingEventsByDateRange() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList);
        
        when(loanTrackingService.getTrackingEventsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(trackingPage);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/events")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
    
    @Test
    void testGetRecentTrackingEvents() throws Exception {
        // Given
        int limit = 50;
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        
        when(loanTrackingService.getRecentTrackingEvents(limit))
            .thenReturn(trackingList);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/events/recent")
                .param("limit", String.valueOf(limit))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void testGetNotificationStatistics() throws Exception {
        // Given
        Map<String, Long> stats = new HashMap<>();
        stats.put("LOAN_CONFIRMATION", 10L);
        stats.put("DUE_REMINDER", 5L);
        
        when(loanTrackingService.getNotificationStatistics()).thenReturn(stats);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/notifications/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOAN_CONFIRMATION").value(10))
                .andExpect(jsonPath("$.DUE_REMINDER").value(5));
    }
    
    @Test
    void testGetLoanTrackingStatistics() throws Exception {
        // Given
        Long loanId = 1L;
        Map<String, Long> stats = new HashMap<>();
        stats.put("NOTIFICATION_SENT", 3L);
        stats.put("STATUS_CHANGE", 1L);
        
        when(loanTrackingService.getLoanTrackingStatistics(loanId)).thenReturn(stats);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/loan/{loanId}/statistics", loanId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NOTIFICATION_SENT").value(3))
                .andExpect(jsonPath("$.STATUS_CHANGE").value(1));
    }
    
    @Test
    void testGetLoanAnalytics() throws Exception {
        // Given
        LoanAnalyticsService.LoanAnalytics analytics = new LoanAnalyticsService.LoanAnalytics();
        analytics.setTotalLoans(100L);
        analytics.setActiveLoans(20L);
        analytics.setOverdueLoans(5L);
        analytics.setReturnedLoans(75L);
        analytics.setOverdueRate(5.0);
        analytics.setReturnRate(75.0);
        
        when(loanAnalyticsService.getLoanAnalytics()).thenReturn(analytics);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/analytics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLoans").value(100))
                .andExpect(jsonPath("$.activeLoans").value(20))
                .andExpect(jsonPath("$.overdueLoans").value(5))
                .andExpect(jsonPath("$.returnedLoans").value(75))
                .andExpect(jsonPath("$.overdueRate").value(5.0))
                .andExpect(jsonPath("$.returnRate").value(75.0));
    }
    
    @Test
    void testGetLoanAnalyticsForDateRange() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        LoanAnalyticsService.LoanAnalytics analytics = new LoanAnalyticsService.LoanAnalytics();
        analytics.setTotalLoans(50L);
        analytics.setActiveLoans(10L);
        
        when(loanAnalyticsService.getLoanAnalyticsForDateRange(startDate, endDate))
            .thenReturn(analytics);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/analytics/date-range")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLoans").value(50))
                .andExpect(jsonPath("$.activeLoans").value(10));
    }
    
    @Test
    void testGetOverdueLoanAnalysis() throws Exception {
        // Given
        LoanAnalyticsService.OverdueLoanAnalysis analysis = new LoanAnalyticsService.OverdueLoanAnalysis();
        analysis.setTotalOverdueLoans(5L);
        analysis.setAverageDaysOverdue(7.5);
        analysis.setLongestOverdueDays(15L);
        
        when(loanAnalyticsService.getOverdueLoanAnalysis()).thenReturn(analysis);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/analytics/overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOverdueLoans").value(5))
                .andExpect(jsonPath("$.averageDaysOverdue").value(7.5))
                .andExpect(jsonPath("$.longestOverdueDays").value(15));
    }
    
    @Test
    void testGetBorrowerAnalysis() throws Exception {
        // Given
        LoanAnalyticsService.BorrowerAnalysis analysis = new LoanAnalyticsService.BorrowerAnalysis();
        analysis.setTotalUniqueBorrowers(25L);
        analysis.setAverageLoansPerBorrower(2.5);
        analysis.setRepeatBorrowers(15L);
        analysis.setRepeatBorrowerRate(60.0);
        
        when(loanAnalyticsService.getBorrowerAnalysis()).thenReturn(analysis);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/analytics/borrowers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUniqueBorrowers").value(25))
                .andExpect(jsonPath("$.averageLoansPerBorrower").value(2.5))
                .andExpect(jsonPath("$.repeatBorrowers").value(15))
                .andExpect(jsonPath("$.repeatBorrowerRate").value(60.0));
    }
    
    @Test
    void testGetDailyNotificationStats() throws Exception {
        // Given
        int days = 30;
        Map<String, Long> stats = new HashMap<>();
        stats.put("2024-01-15", 5L);
        stats.put("2024-01-16", 3L);
        
        when(loanAnalyticsService.getDailyNotificationStats(days)).thenReturn(stats);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/notifications/daily-stats")
                .param("days", String.valueOf(days))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['2024-01-15']").value(5))
                .andExpect(jsonPath("$['2024-01-16']").value(3));
    }
    
    @Test
    void testTriggerOverdueProcessing() throws Exception {
        // Given
        doNothing().when(loanNotificationService).triggerOverdueProcessing();
        
        // When & Then
        mockMvc.perform(post("/api/loan-tracking/notifications/trigger-overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Overdue loan processing triggered successfully"));
        
        verify(loanNotificationService).triggerOverdueProcessing();
    }
    
    @Test
    void testTriggerDueReminders() throws Exception {
        // Given
        doNothing().when(loanNotificationService).triggerDueReminders();
        
        // When & Then
        mockMvc.perform(post("/api/loan-tracking/notifications/trigger-reminders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Due reminder processing triggered successfully"));
        
        verify(loanNotificationService).triggerDueReminders();
    }
    
    @Test
    void testCleanupOldTrackingRecords() throws Exception {
        // Given
        int daysToKeep = 365;
        int deletedRecords = 50;
        
        when(loanTrackingService.cleanupOldTrackingRecords(daysToKeep)).thenReturn(deletedRecords);
        
        // When & Then
        mockMvc.perform(delete("/api/loan-tracking/cleanup")
                .param("daysToKeep", String.valueOf(daysToKeep))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cleanup completed successfully"))
                .andExpect(jsonPath("$.deletedRecords").value(50));
        
        verify(loanTrackingService).cleanupOldTrackingRecords(daysToKeep);
    }
    
    @Test
    void testGetLoanTrackingHistory_WithPagination() throws Exception {
        // Given
        Long loanId = 1L;
        int page = 1;
        int size = 5;
        
        List<LoanTracking> trackingList = Arrays.asList(testTracking);
        Page<LoanTracking> trackingPage = new PageImpl<>(trackingList, PageRequest.of(page, size), 1);
        
        when(loanTrackingService.getLoanTrackingHistory(eq(loanId), any()))
            .thenReturn(trackingPage);
        
        // When & Then
        mockMvc.perform(get("/api/loan-tracking/loan/{loanId}/history", loanId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageNumber").value(page))
                .andExpect(jsonPath("$.pageable.pageSize").value(size));
    }
}