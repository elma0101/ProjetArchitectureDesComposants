package com.bookstore.controller;

import com.bookstore.entity.LoanTracking;
import com.bookstore.service.LoanAnalyticsService;
import com.bookstore.service.LoanNotificationService;
import com.bookstore.service.LoanTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for loan tracking and analytics operations
 */
@RestController
@RequestMapping("/api/loan-tracking")
@Tag(name = "Loan Tracking", description = "Loan tracking, notifications, and analytics operations")
public class LoanTrackingController {
    
    private final LoanTrackingService loanTrackingService;
    private final LoanAnalyticsService loanAnalyticsService;
    private final LoanNotificationService loanNotificationService;
    
    @Autowired
    public LoanTrackingController(LoanTrackingService loanTrackingService,
                                 LoanAnalyticsService loanAnalyticsService,
                                 LoanNotificationService loanNotificationService) {
        this.loanTrackingService = loanTrackingService;
        this.loanAnalyticsService = loanAnalyticsService;
        this.loanNotificationService = loanNotificationService;
    }
    
    /**
     * Get tracking history for a specific loan
     */
    @GetMapping("/loan/{loanId}/history")
    @Operation(
        summary = "Get loan tracking history",
        description = "Retrieve the complete tracking history for a specific loan including all events and notifications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tracking history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Page<LoanTracking>> getLoanTrackingHistory(
            @Parameter(description = "ID of the loan", required = true, example = "1")
            @PathVariable Long loanId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanTracking> history = loanTrackingService.getLoanTrackingHistory(loanId, pageable);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get tracking events by type
     */
    @GetMapping("/events/{eventType}")
    @Operation(
        summary = "Get tracking events by type",
        description = "Retrieve tracking events filtered by event type (e.g., NOTIFICATION_SENT, STATUS_CHANGE, LOAN_EXTENDED)."
    )
    public ResponseEntity<Page<LoanTracking>> getTrackingEventsByType(
            @Parameter(description = "Type of event to filter by", required = true, example = "NOTIFICATION_SENT")
            @PathVariable String eventType,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanTracking> events = loanTrackingService.getTrackingEventsByType(eventType, pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get tracking events by date range
     */
    @GetMapping("/events")
    @Operation(
        summary = "Get tracking events by date range",
        description = "Retrieve tracking events within a specified date range."
    )
    public ResponseEntity<Page<LoanTracking>> getTrackingEventsByDateRange(
            @Parameter(description = "Start date for the range", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for the range", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanTracking> events = loanTrackingService.getTrackingEventsByDateRange(startDateTime, endDateTime, pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get recent tracking events
     */
    @GetMapping("/events/recent")
    @Operation(
        summary = "Get recent tracking events",
        description = "Retrieve the most recent tracking events across all loans."
    )
    public ResponseEntity<List<LoanTracking>> getRecentTrackingEvents(
            @Parameter(description = "Maximum number of events to return", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        
        List<LoanTracking> events = loanTrackingService.getRecentTrackingEvents(limit);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get notification statistics
     */
    @GetMapping("/notifications/statistics")
    @Operation(
        summary = "Get notification statistics",
        description = "Retrieve statistics about notifications sent, grouped by notification type."
    )
    public ResponseEntity<Map<String, Long>> getNotificationStatistics() {
        Map<String, Long> stats = loanTrackingService.getNotificationStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get tracking statistics for a specific loan
     */
    @GetMapping("/loan/{loanId}/statistics")
    @Operation(
        summary = "Get loan tracking statistics",
        description = "Retrieve tracking statistics for a specific loan, showing counts of different event types."
    )
    public ResponseEntity<Map<String, Long>> getLoanTrackingStatistics(
            @Parameter(description = "ID of the loan", required = true, example = "1")
            @PathVariable Long loanId) {
        
        Map<String, Long> stats = loanTrackingService.getLoanTrackingStatistics(loanId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get comprehensive loan analytics
     */
    @GetMapping("/analytics")
    @Operation(
        summary = "Get comprehensive loan analytics",
        description = "Retrieve comprehensive analytics including loan statistics, trends, and borrower behavior."
    )
    public ResponseEntity<LoanAnalyticsService.LoanAnalytics> getLoanAnalytics() {
        LoanAnalyticsService.LoanAnalytics analytics = loanAnalyticsService.getLoanAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get loan analytics for date range
     */
    @GetMapping("/analytics/date-range")
    @Operation(
        summary = "Get loan analytics for date range",
        description = "Retrieve loan analytics for a specific date range."
    )
    public ResponseEntity<LoanAnalyticsService.LoanAnalytics> getLoanAnalyticsForDateRange(
            @Parameter(description = "Start date for the range", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for the range", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LoanAnalyticsService.LoanAnalytics analytics = loanAnalyticsService.getLoanAnalyticsForDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get overdue loan analysis
     */
    @GetMapping("/analytics/overdue")
    @Operation(
        summary = "Get overdue loan analysis",
        description = "Retrieve detailed analysis of overdue loans including average days overdue and distribution."
    )
    public ResponseEntity<LoanAnalyticsService.OverdueLoanAnalysis> getOverdueLoanAnalysis() {
        LoanAnalyticsService.OverdueLoanAnalysis analysis = loanAnalyticsService.getOverdueLoanAnalysis();
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Get borrower behavior analysis
     */
    @GetMapping("/analytics/borrowers")
    @Operation(
        summary = "Get borrower behavior analysis",
        description = "Retrieve analysis of borrower behavior including repeat borrower rates and loan patterns."
    )
    public ResponseEntity<LoanAnalyticsService.BorrowerAnalysis> getBorrowerAnalysis() {
        LoanAnalyticsService.BorrowerAnalysis analysis = loanAnalyticsService.getBorrowerAnalysis();
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Get daily notification statistics
     */
    @GetMapping("/notifications/daily-stats")
    @Operation(
        summary = "Get daily notification statistics",
        description = "Retrieve daily notification counts for the specified number of days."
    )
    public ResponseEntity<Map<String, Long>> getDailyNotificationStats(
            @Parameter(description = "Number of days to include in statistics", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        
        Map<String, Long> stats = loanAnalyticsService.getDailyNotificationStats(days);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Manually trigger overdue loan processing (admin endpoint)
     */
    @PostMapping("/notifications/trigger-overdue")
    @Operation(
        summary = "Trigger overdue loan processing",
        description = "Manually trigger the overdue loan detection and notification process. This is typically run automatically via scheduled tasks."
    )
    public ResponseEntity<Map<String, String>> triggerOverdueProcessing() {
        loanNotificationService.triggerOverdueProcessing();
        return ResponseEntity.ok(Map.of("message", "Overdue loan processing triggered successfully"));
    }
    
    /**
     * Manually trigger due reminders (admin endpoint)
     */
    @PostMapping("/notifications/trigger-reminders")
    @Operation(
        summary = "Trigger due reminders",
        description = "Manually trigger the due reminder notification process. This is typically run automatically via scheduled tasks."
    )
    public ResponseEntity<Map<String, String>> triggerDueReminders() {
        loanNotificationService.triggerDueReminders();
        return ResponseEntity.ok(Map.of("message", "Due reminder processing triggered successfully"));
    }
    
    /**
     * Clean up old tracking records (admin endpoint)
     */
    @DeleteMapping("/cleanup")
    @Operation(
        summary = "Clean up old tracking records",
        description = "Remove tracking records older than the specified number of days to maintain database performance."
    )
    public ResponseEntity<Map<String, Object>> cleanupOldTrackingRecords(
            @Parameter(description = "Number of days to keep tracking records", example = "365")
            @RequestParam(defaultValue = "365") @Min(30) @Max(3650) int daysToKeep) {
        
        int deletedRecords = loanTrackingService.cleanupOldTrackingRecords(daysToKeep);
        return ResponseEntity.ok(Map.of(
            "message", "Cleanup completed successfully",
            "deletedRecords", deletedRecords
        ));
    }
}