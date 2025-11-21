package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import com.bookstore.exception.BookNotAvailableException;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.LoanNotFoundException;
import com.bookstore.exception.InvalidLoanOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanService {
    
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private LoanTrackingService loanTrackingService;
    private LoanNotificationService loanNotificationService;
    
    // Default loan period in days
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    
    @Autowired
    public LoanService(LoanRepository loanRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
    }
    
    @Autowired(required = false)
    public void setLoanTrackingService(LoanTrackingService loanTrackingService) {
        this.loanTrackingService = loanTrackingService;
    }
    
    @Autowired(required = false)
    public void setLoanNotificationService(LoanNotificationService loanNotificationService) {
        this.loanNotificationService = loanNotificationService;
    }
    
    /**
     * Create a new loan for a book
     */
    public Loan borrowBook(Long bookId, String borrowerName, String borrowerEmail, String borrowerId, String notes) {
        // Find the book
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
        
        // Check if book is available
        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("Book is not available for loan: " + book.getTitle());
        }
        
        // Check if book is already loaned to this borrower
        List<Loan> existingActiveLoans = loanRepository.findActiveLoansByBookId(bookId);
        boolean alreadyBorrowedByUser = existingActiveLoans.stream()
            .anyMatch(loan -> loan.getBorrowerEmail().equalsIgnoreCase(borrowerEmail));
        
        if (alreadyBorrowedByUser) {
            throw new InvalidLoanOperationException("Book is already borrowed by this user");
        }
        
        // Create new loan
        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);
        
        Loan loan = new Loan(book, borrowerName, borrowerEmail, loanDate, dueDate);
        loan.setBorrowerId(borrowerId);
        loan.setNotes(notes);
        
        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        // Save loan
        Loan savedLoan = loanRepository.save(loan);
        
        // Record loan creation and send confirmation
        if (loanTrackingService != null) {
            loanTrackingService.recordLoanCreated(savedLoan.getId(), borrowerEmail, bookId);
        }
        if (loanNotificationService != null) {
            loanNotificationService.sendLoanConfirmation(savedLoan);
        }
        
        return savedLoan;
    }
    
    /**
     * Return a borrowed book
     */
    public Loan returnBook(Long loanId, String notes) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new InvalidLoanOperationException("Loan is not active and cannot be returned");
        }
        
        // Mark loan as returned
        loan.markAsReturned();
        if (notes != null && !notes.trim().isEmpty()) {
            loan.setNotes(loan.getNotes() != null ? loan.getNotes() + "; " + notes : notes);
        }
        
        // Increase available copies
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        // Check if return was late
        boolean wasOverdue = loan.getReturnDate().isAfter(loan.getDueDate());
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Record loan return and send confirmation
        if (loanTrackingService != null) {
            loanTrackingService.recordLoanReturned(savedLoan.getId(), wasOverdue);
        }
        if (loanNotificationService != null) {
            loanNotificationService.sendReturnConfirmation(savedLoan);
        }
        
        return savedLoan;
    }
    
    /**
     * Extend loan due date
     */
    public Loan extendLoan(Long loanId, int additionalDays) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidLoanOperationException("Only active loans can be extended");
        }
        
        if (additionalDays <= 0 || additionalDays > 30) {
            throw new InvalidLoanOperationException("Extension days must be between 1 and 30");
        }
        
        LocalDate oldDueDate = loan.getDueDate();
        loan.setDueDate(loan.getDueDate().plusDays(additionalDays));
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Record loan extension
        if (loanTrackingService != null) {
            loanTrackingService.recordLoanExtension(savedLoan.getId(), oldDueDate, savedLoan.getDueDate(), additionalDays);
        }
        
        return savedLoan;
    }
    
    /**
     * Get all loans with pagination
     */
    @Transactional(readOnly = true)
    public Page<Loan> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }
    
    /**
     * Get loan by ID
     */
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanById(Long loanId) {
        return loanRepository.findById(loanId);
    }
    
    /**
     * Get active loans
     */
    @Transactional(readOnly = true)
    public Page<Loan> getActiveLoans(Pageable pageable) {
        return loanRepository.findByStatusOrderByDueDateAsc(LoanStatus.ACTIVE, pageable);
    }
    
    /**
     * Get overdue loans
     */
    @Transactional(readOnly = true)
    public Page<Loan> getOverdueLoans(Pageable pageable) {
        return loanRepository.findOverdueLoans(pageable);
    }
    
    /**
     * Get loans by borrower email
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByBorrowerEmail(String borrowerEmail, Pageable pageable) {
        return loanRepository.findByBorrowerEmailIgnoreCase(borrowerEmail, pageable);
    }
    
    /**
     * Get loans by borrower ID
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByBorrowerId(String borrowerId, Pageable pageable) {
        return loanRepository.findByBorrowerId(borrowerId, pageable);
    }
    
    /**
     * Get loan history for a borrower
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoanHistoryByBorrower(String borrowerEmail, Pageable pageable) {
        return loanRepository.findLoanHistoryByBorrower(borrowerEmail, pageable);
    }
    
    /**
     * Search loans with multiple criteria
     */
    @Transactional(readOnly = true)
    public Page<Loan> searchLoans(String borrowerEmail, String borrowerName, LoanStatus status, 
                                 LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return loanRepository.searchLoans(borrowerEmail, borrowerName, status, startDate, endDate, pageable);
    }
    
    /**
     * Get loans due today
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueToday() {
        return loanRepository.findLoansDueToday();
    }
    
    /**
     * Get loans due within specified days
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueWithinDays(int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        return loanRepository.findLoansDueWithinDays(endDate);
    }
    
    /**
     * Check if a book is currently loaned
     */
    @Transactional(readOnly = true)
    public boolean isBookCurrentlyLoaned(Long bookId) {
        return loanRepository.isBookCurrentlyLoaned(bookId);
    }
    
    /**
     * Get count of active loans by borrower
     */
    @Transactional(readOnly = true)
    public Long getActiveLoanCountByBorrower(String borrowerEmail) {
        return loanRepository.countActiveLoansByBorrower(borrowerEmail);
    }
    
    /**
     * Update overdue loans status
     * This method should be called periodically (e.g., daily) to update loan statuses
     */
    public void updateOverdueLoans() {
        Page<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE, Pageable.unpaged());
        
        for (Loan loan : activeLoans) {
            if (loan.isOverdue() && loan.getStatus() == LoanStatus.ACTIVE) {
                loan.markAsOverdue();
                loanRepository.save(loan);
            }
        }
    }
    
    /**
     * Get loan statistics
     */
    @Transactional(readOnly = true)
    public LoanStatistics getLoanStatistics() {
        Long activeCount = loanRepository.countByStatus(LoanStatus.ACTIVE);
        Long overdueCount = loanRepository.countByStatus(LoanStatus.OVERDUE);
        Long returnedCount = loanRepository.countByStatus(LoanStatus.RETURNED);
        
        return new LoanStatistics(activeCount, overdueCount, returnedCount);
    }
    
    /**
     * Get most borrowed books
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getMostBorrowedBooks(Pageable pageable) {
        return loanRepository.findMostBorrowedBooks(pageable);
    }
    
    /**
     * Get most active borrowers
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getMostActiveBorrowers(Pageable pageable) {
        return loanRepository.findMostActiveBorrowers(pageable);
    }
    
    /**
     * Inner class for loan statistics
     */
    public static class LoanStatistics {
        private final Long activeLoans;
        private final Long overdueLoans;
        private final Long returnedLoans;
        private final Long totalLoans;
        
        public LoanStatistics(Long activeLoans, Long overdueLoans, Long returnedLoans) {
            this.activeLoans = activeLoans != null ? activeLoans : 0L;
            this.overdueLoans = overdueLoans != null ? overdueLoans : 0L;
            this.returnedLoans = returnedLoans != null ? returnedLoans : 0L;
            this.totalLoans = this.activeLoans + this.overdueLoans + this.returnedLoans;
        }
        
        public Long getActiveLoans() { return activeLoans; }
        public Long getOverdueLoans() { return overdueLoans; }
        public Long getReturnedLoans() { return returnedLoans; }
        public Long getTotalLoans() { return totalLoans; }
    }
}