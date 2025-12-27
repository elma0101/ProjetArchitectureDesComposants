package com.bookstore.loanmanagement.controller;

import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.dto.LoanResponse;
import com.bookstore.loanmanagement.dto.LoanStatistics;
import com.bookstore.loanmanagement.dto.ReturnLoanRequest;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.entity.LoanTracking;
import com.bookstore.loanmanagement.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Loan Management operations
 * Provides endpoints for loan creation, returns, search, and analytics
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    /**
     * Create a new loan
     * POST /api/loans
     */
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        log.info("Creating loan for userId={}, bookId={}", request.getUserId(), request.getBookId());
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Return a borrowed book
     * PUT /api/loans/{id}/return
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnLoanRequest request) {
        log.info("Processing return for loanId={}", id);
        String notes = request != null ? request.getNotes() : null;
        LoanResponse response = loanService.returnLoan(id, notes);
        return ResponseEntity.ok(response);
    }

    /**
     * Extend loan due date
     * PUT /api/loans/{id}/extend
     */
    @PutMapping("/{id}/extend")
    public ResponseEntity<LoanResponse> extendLoan(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") int days) {
        log.info("Extending loan: loanId={}, days={}", id, days);
        LoanResponse response = loanService.extendLoan(id, days);
        return ResponseEntity.ok(response);
    }

    /**
     * Get loan by ID
     * GET /api/loans/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        log.info("Fetching loan: loanId={}", id);
        LoanResponse response = loanService.getLoanById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get loan history/tracking for a specific loan
     * GET /api/loans/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<LoanTracking>> getLoanHistory(@PathVariable Long id) {
        log.info("Fetching loan history: loanId={}", id);
        List<LoanTracking> history = loanService.getLoanHistory(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Get all loans for a user with pagination
     * GET /api/loans/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LoanResponse>> getUserLoans(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching loans for userId={}", userId);
        Page<LoanResponse> loans = loanService.getLoansByUserId(userId, pageable);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get active loans for a user
     * GET /api/loans/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<LoanResponse>> getActiveUserLoans(@PathVariable Long userId) {
        log.info("Fetching active loans for userId={}", userId);
        List<LoanResponse> loans = loanService.getActiveLoansForUser(userId);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get all loans for a book
     * GET /api/loans/book/{bookId}
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<LoanResponse>> getBookLoans(@PathVariable Long bookId) {
        log.info("Fetching loans for bookId={}", bookId);
        List<LoanResponse> loans = loanService.getLoansByBookId(bookId);
        return ResponseEntity.ok(loans);
    }

    /**
     * Search and filter loans
     * GET /api/loans/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<LoanResponse>> searchLoans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Boolean overdue) {
        log.info("Searching loans with filters: userId={}, bookId={}, status={}, overdue={}", 
                userId, bookId, status, overdue);
        
        List<LoanResponse> loans = loanService.searchLoans(userId, bookId, status, fromDate, toDate, overdue);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get overdue loans
     * GET /api/loans/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        log.info("Fetching overdue loans");
        List<LoanResponse> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(loans);
    }

    /**
     * Update overdue loan statuses (admin operation)
     * POST /api/loans/overdue/update
     */
    @PostMapping("/overdue/update")
    public ResponseEntity<Integer> updateOverdueLoans() {
        log.info("Updating overdue loan statuses");
        int updatedCount = loanService.updateOverdueLoans();
        return ResponseEntity.ok(updatedCount);
    }

    /**
     * Get loan statistics
     * GET /api/loans/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<LoanStatistics> getLoanStatistics() {
        log.info("Fetching loan statistics");
        LoanStatistics stats = loanService.getLoanStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get loan analytics for a user
     * GET /api/loans/analytics/user/{userId}
     */
    @GetMapping("/analytics/user/{userId}")
    public ResponseEntity<Long> getUserLoanCount(@PathVariable Long userId) {
        log.info("Fetching active loan count for userId={}", userId);
        long count = loanService.getActiveLoanCountForUser(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get loan analytics for a book
     * GET /api/loans/analytics/book/{bookId}
     */
    @GetMapping("/analytics/book/{bookId}")
    public ResponseEntity<Long> getBookLoanCount(@PathVariable Long bookId) {
        log.info("Fetching active loan count for bookId={}", bookId);
        long count = loanService.getActiveLoanCountForBook(bookId);
        return ResponseEntity.ok(count);
    }
}
