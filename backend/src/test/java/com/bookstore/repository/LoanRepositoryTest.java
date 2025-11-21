package com.bookstore.repository;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LoanRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private LoanRepository loanRepository;
    
    private Book testBook1;
    private Book testBook2;
    private Loan activeLoan;
    private Loan overdueLoan;
    private Loan returnedLoan;
    
    @BeforeEach
    void setUp() {
        // Create test books
        testBook1 = new Book("Test Book 1", "978-0-452-28423-4");
        testBook1 = entityManager.persistAndFlush(testBook1);
        
        testBook2 = new Book("Test Book 2", "978-0-452-28424-1");
        testBook2 = entityManager.persistAndFlush(testBook2);
        
        // Create test loans
        activeLoan = new Loan(testBook1, "John Doe", "john.doe@email.com", 
                             LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));
        activeLoan.setBorrowerId("USER001");
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan = entityManager.persistAndFlush(activeLoan);
        
        overdueLoan = new Loan(testBook2, "Jane Smith", "jane.smith@email.com", 
                              LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        overdueLoan.setBorrowerId("USER002");
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        overdueLoan = entityManager.persistAndFlush(overdueLoan);
        
        returnedLoan = new Loan(testBook1, "Bob Johnson", "bob.johnson@email.com", 
                               LocalDate.now().minusDays(30), LocalDate.now().minusDays(16));
        returnedLoan.setBorrowerId("USER003");
        returnedLoan.setStatus(LoanStatus.RETURNED);
        returnedLoan.setReturnDate(LocalDate.now().minusDays(18));
        returnedLoan = entityManager.persistAndFlush(returnedLoan);
        
        entityManager.clear();
    }
    
    @Test
    void testFindByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE, pageable);
        assertEquals(2, activeLoans.getTotalElements());
        
        Page<Loan> returnedLoans = loanRepository.findByStatus(LoanStatus.RETURNED, pageable);
        assertEquals(1, returnedLoans.getTotalElements());
        
        Page<Loan> overdueLoans = loanRepository.findByStatus(LoanStatus.OVERDUE, pageable);
        assertEquals(0, overdueLoans.getTotalElements());
    }
    
    @Test
    void testFindByStatusOrderByDueDateAsc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findByStatusOrderByDueDateAsc(LoanStatus.ACTIVE, pageable);
        
        assertEquals(2, result.getTotalElements());
        List<Loan> loans = result.getContent();
        
        // Should be ordered by due date ascending
        assertTrue(loans.get(0).getDueDate().isBefore(loans.get(1).getDueDate()) ||
                  loans.get(0).getDueDate().isEqual(loans.get(1).getDueDate()));
    }
    
    @Test
    void testFindOverdueLoans() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findOverdueLoans(pageable);
        
        assertEquals(1, result.getTotalElements());
        Loan overdue = result.getContent().get(0);
        assertEquals("Jane Smith", overdue.getBorrowerName());
        assertTrue(overdue.getDueDate().isBefore(LocalDate.now()));
    }
    
    @Test
    void testFindByBorrowerEmailIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Loan> result = loanRepository.findByBorrowerEmailIgnoreCase("john.doe@email.com", pageable);
        assertEquals(1, result.getTotalElements());
        
        // Test case insensitive
        result = loanRepository.findByBorrowerEmailIgnoreCase("JOHN.DOE@EMAIL.COM", pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindByBorrowerId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findByBorrowerId("USER001", pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getBorrowerName());
    }
    
    @Test
    void testFindByBorrowerNameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Loan> result = loanRepository.findByBorrowerNameContainingIgnoreCase("john", pageable);
        assertEquals(2, result.getTotalElements()); // John Doe and Bob Johnson
        
        result = loanRepository.findByBorrowerNameContainingIgnoreCase("JOHN", pageable);
        assertEquals(2, result.getTotalElements()); // Case insensitive
        
        result = loanRepository.findByBorrowerNameContainingIgnoreCase("jane", pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindByBookId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findByBookId(testBook1.getId(), pageable);
        
        assertEquals(2, result.getTotalElements()); // activeLoan and returnedLoan
        
        result = loanRepository.findByBookId(testBook2.getId(), pageable);
        assertEquals(1, result.getTotalElements()); // overdueLoan
    }
    
    @Test
    void testFindByBookTitleContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findByBookTitleContainingIgnoreCase("test book 1", pageable);
        
        assertEquals(2, result.getTotalElements());
        
        result = loanRepository.findByBookTitleContainingIgnoreCase("TEST BOOK", pageable);
        assertEquals(3, result.getTotalElements()); // All loans (case insensitive)
    }
    
    @Test
    void testFindByLoanDateBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        
        Page<Loan> result = loanRepository.findByLoanDateBetween(startDate, endDate, pageable);
        assertEquals(1, result.getTotalElements()); // Only activeLoan
        
        startDate = LocalDate.now().minusDays(35);
        endDate = LocalDate.now().minusDays(15);
        result = loanRepository.findByLoanDateBetween(startDate, endDate, pageable);
        assertEquals(2, result.getTotalElements()); // overdueLoan and returnedLoan
    }
    
    @Test
    void testFindByDueDateBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);
        
        Page<Loan> result = loanRepository.findByDueDateBetween(startDate, endDate, pageable);
        assertEquals(2, result.getTotalElements()); // activeLoan and overdueLoan
    }
    
    @Test
    void testFindLoansDueToday() {
        // Create a loan due today
        Loan dueTodayLoan = new Loan(testBook1, "Test User", "test@email.com", 
                                   LocalDate.now().minusDays(14), LocalDate.now());
        dueTodayLoan.setStatus(LoanStatus.ACTIVE);
        entityManager.persistAndFlush(dueTodayLoan);
        
        List<Loan> result = loanRepository.findLoansDueToday();
        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).getBorrowerName());
    }
    
    @Test
    void testFindLoansDueWithinDays() {
        LocalDate endDate = LocalDate.now().plusDays(10);
        List<Loan> result = loanRepository.findLoansDueWithinDays(endDate);
        
        assertEquals(1, result.size()); // Only activeLoan is due within 10 days
        assertEquals("John Doe", result.get(0).getBorrowerName());
    }
    
    @Test
    void testIsBookCurrentlyLoaned() {
        assertTrue(loanRepository.isBookCurrentlyLoaned(testBook1.getId()));
        assertTrue(loanRepository.isBookCurrentlyLoaned(testBook2.getId()));
        
        // Test with non-existent book
        assertFalse(loanRepository.isBookCurrentlyLoaned(999L));
    }
    
    @Test
    void testFindActiveLoansByBookId() {
        List<Loan> result = loanRepository.findActiveLoansByBookId(testBook1.getId());
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getBorrowerName());
        
        result = loanRepository.findActiveLoansByBookId(testBook2.getId());
        assertEquals(1, result.size());
        assertEquals("Jane Smith", result.get(0).getBorrowerName());
    }
    
    @Test
    void testCountActiveLoansByBorrower() {
        Long count = loanRepository.countActiveLoansByBorrower("john.doe@email.com");
        assertEquals(1L, count);
        
        count = loanRepository.countActiveLoansByBorrower("bob.johnson@email.com");
        assertEquals(0L, count); // Bob's loan is returned
        
        count = loanRepository.countActiveLoansByBorrower("nonexistent@email.com");
        assertEquals(0L, count);
    }
    
    @Test
    void testFindLoanHistoryByBorrower() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findLoanHistoryByBorrower("john.doe@email.com", pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getBorrowerName());
    }
    
    @Test
    void testSearchLoans() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Search by borrower email
        Page<Loan> result = loanRepository.searchLoans("john.doe", null, null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search by borrower name
        result = loanRepository.searchLoans(null, "jane", null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search by status
        result = loanRepository.searchLoans(null, null, LoanStatus.ACTIVE, null, null, pageable);
        assertEquals(2, result.getTotalElements());
        
        // Search by date range
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        result = loanRepository.searchLoans(null, null, null, startDate, endDate, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Combined search
        result = loanRepository.searchLoans("john", "john", LoanStatus.ACTIVE, null, null, pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindLateReturns() {
        // Create a late return loan
        Loan lateReturnLoan = new Loan(testBook1, "Late User", "late@email.com", 
                                     LocalDate.now().minusDays(30), LocalDate.now().minusDays(16));
        lateReturnLoan.setStatus(LoanStatus.RETURNED);
        lateReturnLoan.setReturnDate(LocalDate.now().minusDays(10)); // Returned 6 days late
        entityManager.persistAndFlush(lateReturnLoan);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findLateReturns(pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Late User", result.getContent().get(0).getBorrowerName());
    }
    
    @Test
    void testCountByStatus() {
        Long activeCount = loanRepository.countByStatus(LoanStatus.ACTIVE);
        assertEquals(2L, activeCount);
        
        Long returnedCount = loanRepository.countByStatus(LoanStatus.RETURNED);
        assertEquals(1L, returnedCount);
        
        Long overdueCount = loanRepository.countByStatus(LoanStatus.OVERDUE);
        assertEquals(0L, overdueCount);
    }
    
    @Test
    void testFindRecentLoans() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> result = loanRepository.findRecentLoans(pageable);
        
        assertEquals(3, result.getTotalElements());
        // Should be ordered by creation date descending
    }
}