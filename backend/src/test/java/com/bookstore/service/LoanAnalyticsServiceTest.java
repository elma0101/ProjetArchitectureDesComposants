package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.LoanRepository;
import com.bookstore.repository.LoanTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanAnalyticsServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private LoanTrackingRepository loanTrackingRepository;
    
    @InjectMocks
    private LoanAnalyticsService loanAnalyticsService;
    
    private Loan activeLoan;
    private Loan overdueLoan;
    private Loan returnedLoan;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        
        activeLoan = new Loan();
        activeLoan.setId(1L);
        activeLoan.setBook(testBook);
        activeLoan.setBorrowerName("John Doe");
        activeLoan.setBorrowerEmail("john.doe@email.com");
        activeLoan.setLoanDate(LocalDate.now().minusDays(5));
        activeLoan.setDueDate(LocalDate.now().plusDays(9));
        activeLoan.setStatus(LoanStatus.ACTIVE);
        
        overdueLoan = new Loan();
        overdueLoan.setId(2L);
        overdueLoan.setBook(testBook);
        overdueLoan.setBorrowerName("Jane Smith");
        overdueLoan.setBorrowerEmail("jane.smith@email.com");
        overdueLoan.setLoanDate(LocalDate.now().minusDays(20));
        overdueLoan.setDueDate(LocalDate.now().minusDays(5));
        overdueLoan.setStatus(LoanStatus.OVERDUE);
        
        returnedLoan = new Loan();
        returnedLoan.setId(3L);
        returnedLoan.setBook(testBook);
        returnedLoan.setBorrowerName("Bob Johnson");
        returnedLoan.setBorrowerEmail("bob.johnson@email.com");
        returnedLoan.setLoanDate(LocalDate.now().minusDays(30));
        returnedLoan.setDueDate(LocalDate.now().minusDays(16));
        returnedLoan.setReturnDate(LocalDate.now().minusDays(18));
        returnedLoan.setStatus(LoanStatus.RETURNED);
    }
    
    @Test
    void testGetLoanAnalytics() {
        // Given
        when(loanRepository.count()).thenReturn(3L);
        when(loanRepository.countByStatus(LoanStatus.ACTIVE)).thenReturn(1L);
        when(loanRepository.countByStatus(LoanStatus.OVERDUE)).thenReturn(1L);
        when(loanRepository.countByStatus(LoanStatus.RETURNED)).thenReturn(1L);
        
        // Mock returned loans for average duration calculation
        Page<Loan> returnedLoans = new PageImpl<>(Arrays.asList(returnedLoan));
        when(loanRepository.findByStatus(eq(LoanStatus.RETURNED), any(Pageable.class)))
            .thenReturn(returnedLoans);
        
        // Mock most borrowed books
        Object[] bookStat = {1L, "Test Book", 5L};
        List<Object[]> bookStatsList = new java.util.ArrayList<>();
        bookStatsList.add(bookStat);
        Page<Object[]> mostBorrowedBooks = new PageImpl<Object[]>(bookStatsList);
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(mostBorrowedBooks);
        
        // Mock most active borrowers
        Object[] borrowerStat = {"john.doe@email.com", "John Doe", 3L};
        List<Object[]> borrowerStatsList = new java.util.ArrayList<>();
        borrowerStatsList.add(borrowerStat);
        Page<Object[]> mostActiveBorrowers = new PageImpl<Object[]>(borrowerStatsList);
        when(loanRepository.findMostActiveBorrowers(any(PageRequest.class)))
            .thenReturn(mostActiveBorrowers);
        
        // Mock loan trends
        Page<Loan> recentLoans = new PageImpl<>(Arrays.asList(activeLoan, overdueLoan));
        when(loanRepository.findByLoanDateBetween(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(recentLoans);
        
        // Mock notification stats
        List<Object[]> notificationStatsData = new java.util.ArrayList<>();
        notificationStatsData.add(new Object[]{"LOAN_CONFIRMATION", 10L});
        when(loanTrackingRepository.getNotificationStatistics()).thenReturn(notificationStatsData);
        
        // When
        LoanAnalyticsService.LoanAnalytics analytics = loanAnalyticsService.getLoanAnalytics();
        
        // Then
        assertNotNull(analytics);
        assertEquals(3L, analytics.getTotalLoans());
        assertEquals(1L, analytics.getActiveLoans());
        assertEquals(1L, analytics.getOverdueLoans());
        assertEquals(1L, analytics.getReturnedLoans());
        assertEquals(33.33, analytics.getOverdueRate(), 0.01);
        assertEquals(33.33, analytics.getReturnRate(), 0.01);
        assertNotNull(analytics.getAverageLoanDuration());
        assertNotNull(analytics.getMostBorrowedBooks());
        assertNotNull(analytics.getMostActiveBorrowers());
        assertNotNull(analytics.getLoanTrends());
        assertNotNull(analytics.getNotificationStats());
    }
    
    @Test
    void testGetLoanAnalyticsForDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<Loan> loansInRange = Arrays.asList(activeLoan, overdueLoan, returnedLoan);
        Page<Loan> loansPage = new PageImpl<>(loansInRange);
        
        when(loanRepository.findByLoanDateBetween(startDate, endDate, Pageable.unpaged()))
            .thenReturn(loansPage);
        
        // When
        LoanAnalyticsService.LoanAnalytics analytics = loanAnalyticsService.getLoanAnalyticsForDateRange(startDate, endDate);
        
        // Then
        assertNotNull(analytics);
        assertEquals(3L, analytics.getTotalLoans());
        assertEquals(1L, analytics.getActiveLoans());
        assertEquals(1L, analytics.getOverdueLoans());
        assertEquals(1L, analytics.getReturnedLoans());
    }
    
    @Test
    void testGetOverdueLoanAnalysis() {
        // Given
        List<Loan> overdueLoans = Arrays.asList(overdueLoan);
        Page<Loan> overduePage = new PageImpl<>(overdueLoans);
        Page<Loan> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(loanRepository.findOverdueLoans(Pageable.unpaged())).thenReturn(emptyPage);
        when(loanRepository.findByStatus(LoanStatus.OVERDUE, Pageable.unpaged())).thenReturn(overduePage);
        
        // When
        LoanAnalyticsService.OverdueLoanAnalysis analysis = loanAnalyticsService.getOverdueLoanAnalysis();
        
        // Then
        assertNotNull(analysis);
        assertEquals(1L, analysis.getTotalOverdueLoans());
        assertTrue(analysis.getAverageDaysOverdue() > 0);
        assertTrue(analysis.getLongestOverdueDays() > 0);
        assertNotNull(analysis.getOverdueRanges());
    }
    
    @Test
    void testGetOverdueLoanAnalysis_NoOverdueLoans() {
        // Given
        Page<Loan> emptyPage = new PageImpl<>(Arrays.asList());
        when(loanRepository.findOverdueLoans(Pageable.unpaged())).thenReturn(emptyPage);
        when(loanRepository.findByStatus(LoanStatus.OVERDUE, Pageable.unpaged())).thenReturn(emptyPage);
        
        // When
        LoanAnalyticsService.OverdueLoanAnalysis analysis = loanAnalyticsService.getOverdueLoanAnalysis();
        
        // Then
        assertNotNull(analysis);
        assertEquals(0L, analysis.getTotalOverdueLoans());
        assertNull(analysis.getAverageDaysOverdue());
        assertNull(analysis.getLongestOverdueDays());
        assertNull(analysis.getOverdueRanges());
    }
    
    @Test
    void testGetBorrowerAnalysis() {
        // Given
        List<Loan> allLoans = Arrays.asList(activeLoan, overdueLoan, returnedLoan);
        when(loanRepository.findAll()).thenReturn(allLoans);
        when(loanRepository.count()).thenReturn(3L);
        
        // When
        LoanAnalyticsService.BorrowerAnalysis analysis = loanAnalyticsService.getBorrowerAnalysis();
        
        // Then
        assertNotNull(analysis);
        assertEquals(3L, analysis.getTotalUniqueBorrowers()); // 3 different borrowers
        assertEquals(1.0, analysis.getAverageLoansPerBorrower(), 0.01);
        assertEquals(0L, analysis.getRepeatBorrowers()); // No repeat borrowers in this test
        assertEquals(0.0, analysis.getRepeatBorrowerRate(), 0.01);
    }
    
    @Test
    void testGetBorrowerAnalysis_WithRepeatBorrowers() {
        // Given
        Loan anotherLoanBySameUser = new Loan();
        anotherLoanBySameUser.setId(4L);
        anotherLoanBySameUser.setBook(testBook);
        anotherLoanBySameUser.setBorrowerName("John Doe");
        anotherLoanBySameUser.setBorrowerEmail("john.doe@email.com"); // Same as activeLoan
        anotherLoanBySameUser.setStatus(LoanStatus.RETURNED);
        
        List<Loan> allLoans = Arrays.asList(activeLoan, overdueLoan, returnedLoan, anotherLoanBySameUser);
        when(loanRepository.findAll()).thenReturn(allLoans);
        when(loanRepository.count()).thenReturn(4L);
        
        // When
        LoanAnalyticsService.BorrowerAnalysis analysis = loanAnalyticsService.getBorrowerAnalysis();
        
        // Then
        assertNotNull(analysis);
        assertEquals(3L, analysis.getTotalUniqueBorrowers()); // Still 3 unique borrowers
        assertEquals(1.33, analysis.getAverageLoansPerBorrower(), 0.01);
        assertEquals(1L, analysis.getRepeatBorrowers()); // John Doe has 2 loans
        assertEquals(33.33, analysis.getRepeatBorrowerRate(), 0.01);
    }
    
    @Test
    void testGetDailyNotificationStats() {
        // Given
        int days = 7;
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Object[]> dailyCounts = Arrays.asList(
            new Object[]{"2024-01-15", 5L},
            new Object[]{"2024-01-16", 3L},
            new Object[]{"2024-01-17", 7L}
        );
        
        when(loanTrackingRepository.getDailyNotificationCounts(any(LocalDateTime.class)))
            .thenReturn(dailyCounts);
        
        // When
        Map<String, Long> result = loanAnalyticsService.getDailyNotificationStats(days);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5L, result.get("2024-01-15"));
        assertEquals(3L, result.get("2024-01-16"));
        assertEquals(7L, result.get("2024-01-17"));
    }
    
    @Test
    void testGetLoanAnalytics_ZeroLoans() {
        // Given
        when(loanRepository.count()).thenReturn(0L);
        when(loanRepository.countByStatus(any(LoanStatus.class))).thenReturn(0L);
        
        Page<Loan> emptyPage = new PageImpl<>(Arrays.asList());
        when(loanRepository.findByStatus(eq(LoanStatus.RETURNED), any(Pageable.class)))
            .thenReturn(emptyPage);
        List<Object[]> emptyObjectList = new java.util.ArrayList<>();
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(new PageImpl<Object[]>(emptyObjectList));
        when(loanRepository.findMostActiveBorrowers(any(PageRequest.class)))
            .thenReturn(new PageImpl<Object[]>(emptyObjectList));
        when(loanRepository.findByLoanDateBetween(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(emptyPage);
        List<Object[]> emptyNotificationStats = new java.util.ArrayList<>();
        when(loanTrackingRepository.getNotificationStatistics()).thenReturn(emptyNotificationStats);
        
        // When
        LoanAnalyticsService.LoanAnalytics analytics = loanAnalyticsService.getLoanAnalytics();
        
        // Then
        assertNotNull(analytics);
        assertEquals(0L, analytics.getTotalLoans());
        assertEquals(0L, analytics.getActiveLoans());
        assertEquals(0L, analytics.getOverdueLoans());
        assertEquals(0L, analytics.getReturnedLoans());
        assertNull(analytics.getOverdueRate());
        assertNull(analytics.getReturnRate());
    }
}