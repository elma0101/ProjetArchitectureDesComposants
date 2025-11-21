package com.bookstore.controller;

import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/loan-management")
@Tag(name = "Loans", description = "Loan management operations for book borrowing and returns")
public class LoanController {
    
    private final LoanService loanService;
    
    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    
    /**
     * Borrow a book (create a new loan)
     */
    @PostMapping("/borrow")
    @Operation(
        summary = "Borrow a book",
        description = "Create a new loan record for borrowing a book. The book must be available (have available copies > 0)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book borrowed successfully", 
                    content = @Content(schema = @Schema(implementation = Loan.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or book not available"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Loan> borrowBook(
            @Parameter(description = "Borrow book request details", required = true)
            @Valid @RequestBody BorrowBookRequest request) {
        Loan loan = loanService.borrowBook(
            request.getBookId(),
            request.getBorrowerName(),
            request.getBorrowerEmail(),
            request.getBorrowerId(),
            request.getNotes()
        );
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }
    
    /**
     * Return a book
     */
    @PutMapping("/{loanId}/return")
    @Operation(
        summary = "Return a borrowed book",
        description = "Mark a loan as returned and update the return date. This will also increase the available copies of the book."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book returned successfully"),
        @ApiResponse(responseCode = "404", description = "Loan not found"),
        @ApiResponse(responseCode = "400", description = "Loan already returned or invalid state")
    })
    public ResponseEntity<Loan> returnBook(
            @Parameter(description = "ID of the loan to return", required = true, example = "1")
            @PathVariable Long loanId,
            @Parameter(description = "Optional return notes")
            @RequestBody(required = false) ReturnBookRequest request) {
        
        String notes = request != null ? request.getNotes() : null;
        Loan loan = loanService.returnBook(loanId, notes);
        return ResponseEntity.ok(loan);
    }
    
    /**
     * Extend a loan
     */
    @PutMapping("/{loanId}/extend")
    public ResponseEntity<Loan> extendLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody ExtendLoanRequest request) {
        
        Loan loan = loanService.extendLoan(loanId, request.getAdditionalDays());
        return ResponseEntity.ok(loan);
    }
    
    /**
     * Get all active loans
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Loan>> getActiveLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Loan> loans = loanService.getActiveLoans(pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Get all overdue loans
     */
    @GetMapping("/overdue")
    @Operation(
        summary = "Get overdue loans",
        description = "Retrieve all loans that are past their due date and have not been returned."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue loans retrieved successfully")
    })
    public ResponseEntity<Page<Loan>> getOverdueLoans(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", example = "dueDate")
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Loan> loans = loanService.getOverdueLoans(pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Get loans by borrower email
     */
    @GetMapping("/borrower/{borrowerEmail}")
    public ResponseEntity<Page<Loan>> getLoansByBorrowerEmail(
            @PathVariable String borrowerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "loanDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Loan> loans = loanService.getLoansByBorrowerEmail(borrowerEmail, pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Get loan history for a borrower
     */
    @GetMapping("/history/{borrowerEmail}")
    public ResponseEntity<Page<Loan>> getLoanHistory(
            @PathVariable String borrowerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loans = loanService.getLoanHistoryByBorrower(borrowerEmail, pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Search loans with multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Loan>> searchLoans(
            @RequestParam(required = false) String borrowerEmail,
            @RequestParam(required = false) String borrowerName,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "loanDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Loan> loans = loanService.searchLoans(
            borrowerEmail, borrowerName, status, startDate, endDate, pageable);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Get loans due today
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<Loan>> getLoansDueToday() {
        List<Loan> loans = loanService.getLoansDueToday();
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Get loans due within specified days
     */
    @GetMapping("/due-within")
    public ResponseEntity<List<Loan>> getLoansDueWithin(
            @RequestParam(defaultValue = "7") int days) {
        
        List<Loan> loans = loanService.getLoansDueWithinDays(days);
        return ResponseEntity.ok(loans);
    }
    
    /**
     * Check if a book is currently loaned
     */
    @GetMapping("/book/{bookId}/status")
    public ResponseEntity<Map<String, Object>> getBookLoanStatus(@PathVariable Long bookId) {
        boolean isLoaned = loanService.isBookCurrentlyLoaned(bookId);
        
        Map<String, Object> response = Map.of(
            "bookId", bookId,
            "isCurrentlyLoaned", isLoaned,
            "status", isLoaned ? "LOANED" : "AVAILABLE"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get loan statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<LoanService.LoanStatistics> getLoanStatistics() {
        LoanService.LoanStatistics statistics = loanService.getLoanStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get most borrowed books
     */
    @GetMapping("/statistics/most-borrowed-books")
    public ResponseEntity<Page<Object[]>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> books = loanService.getMostBorrowedBooks(pageable);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get most active borrowers
     */
    @GetMapping("/statistics/most-active-borrowers")
    public ResponseEntity<Page<Object[]>> getMostActiveBorrowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> borrowers = loanService.getMostActiveBorrowers(pageable);
        return ResponseEntity.ok(borrowers);
    }
    
    /**
     * Update overdue loans (admin endpoint)
     */
    @PostMapping("/update-overdue")
    public ResponseEntity<Map<String, String>> updateOverdueLoans() {
        loanService.updateOverdueLoans();
        return ResponseEntity.ok(Map.of("message", "Overdue loans updated successfully"));
    }
    
    /**
     * Get loan by ID
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long loanId) {
        Optional<Loan> loan = loanService.getLoanById(loanId);
        return loan.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    // Request DTOs
    public static class BorrowBookRequest {
        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        private Long bookId;
        
        @NotBlank(message = "Borrower name is required")
        private String borrowerName;
        
        @NotBlank(message = "Borrower email is required")
        @Email(message = "Invalid email format")
        private String borrowerEmail;
        
        private String borrowerId;
        private String notes;
        
        // Getters and setters
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        
        public String getBorrowerName() { return borrowerName; }
        public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
        
        public String getBorrowerEmail() { return borrowerEmail; }
        public void setBorrowerEmail(String borrowerEmail) { this.borrowerEmail = borrowerEmail; }
        
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class ReturnBookRequest {
        private String notes;
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    public static class ExtendLoanRequest {
        @NotNull(message = "Additional days is required")
        @Positive(message = "Additional days must be positive")
        private Integer additionalDays;
        
        public Integer getAdditionalDays() { return additionalDays; }
        public void setAdditionalDays(Integer additionalDays) { this.additionalDays = additionalDays; }
    }
}