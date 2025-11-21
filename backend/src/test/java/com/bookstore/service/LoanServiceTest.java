package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.exception.BookNotAvailableException;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.InvalidLoanOperationException;
import com.bookstore.exception.LoanNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @InjectMocks
    private LoanService loanService;
    
    private Book testBook;
    private Loan testLoan;
    
    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0-123456-78-9");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);
        
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setBook(testBook);
        testLoan.setBorrowerName("John Doe");
        testLoan.setBorrowerEmail("john.doe@example.com");
        testLoan.setBorrowerId("USER001");
        testLoan.setLoanDate(LocalDate.now());
        testLoan.setDueDate(LocalDate.now().plusDays(14));
        testLoan.setStatus(LoanStatus.ACTIVE);
    }
    
    @Test
    void borrowBook_Success() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoansByBookId(1L)).thenReturn(Arrays.asList());
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // When
        Loan result = loanService.borrowBook(1L, "John Doe", "john.doe@example.com", "USER001", "Test notes");
        
        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getBorrowerName());
        assertEquals("john.doe@example.com", result.getBorrowerEmail());
        assertEquals("USER001", result.getBorrowerId());
        assertEquals(LoanStatus.ACTIVE, result.getStatus());
        
        verify(bookRepository).save(argThat(book -> book.getAvailableCopies() == 2));
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void borrowBook_BookNotFound() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BookNotFoundException.class, () -> 
            loanService.borrowBook(1L, "John Doe", "john.doe@example.com", "USER001", null));
        
        verify(loanRepository, never()).save(any(Loan.class));
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void borrowBook_BookNotAvailable() {
        // Given
        testBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // When & Then
        assertThrows(BookNotAvailableException.class, () -> 
            loanService.borrowBook(1L, "John Doe", "john.doe@example.com", "USER001", null));
        
        verify(loanRepository, never()).save(any(Loan.class));
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void borrowBook_AlreadyBorrowedByUser() {
        // Given
        Loan existingLoan = new Loan();
        existingLoan.setBorrowerEmail("john.doe@example.com");
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoansByBookId(1L)).thenReturn(Arrays.asList(existingLoan));
        
        // When & Then
        assertThrows(InvalidLoanOperationException.class, () -> 
            loanService.borrowBook(1L, "John Doe", "john.doe@example.com", "USER001", null));
        
        verify(loanRepository, never()).save(any(Loan.class));
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void returnBook_Success() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // When
        Loan result = loanService.returnBook(1L, "Returned in good condition");
        
        // Then
        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        
        verify(bookRepository).save(argThat(book -> book.getAvailableCopies() == 4));
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void returnBook_LoanNotFound() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(LoanNotFoundException.class, () -> 
            loanService.returnBook(1L, null));
        
        verify(bookRepository, never()).save(any(Book.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
    
    @Test
    void returnBook_LoanAlreadyReturned() {
        // Given
        testLoan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // When & Then
        assertThrows(InvalidLoanOperationException.class, () -> 
            loanService.returnBook(1L, null));
        
        verify(bookRepository, never()).save(any(Book.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
    
    @Test
    void extendLoan_Success() {
        // Given
        LocalDate originalDueDate = testLoan.getDueDate();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        
        // When
        Loan result = loanService.extendLoan(1L, 7);
        
        // Then
        assertNotNull(result);
        assertEquals(originalDueDate.plusDays(7), result.getDueDate());
        
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void extendLoan_LoanNotFound() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(LoanNotFoundException.class, () -> 
            loanService.extendLoan(1L, 7));
        
        verify(loanRepository, never()).save(any(Loan.class));
    }
    
    @Test
    void extendLoan_LoanNotActive() {
        // Given
        testLoan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // When & Then
        assertThrows(InvalidLoanOperationException.class, () -> 
            loanService.extendLoan(1L, 7));
        
        verify(loanRepository, never()).save(any(Loan.class));
    }
    
    @Test
    void extendLoan_InvalidDays() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // When & Then
        assertThrows(InvalidLoanOperationException.class, () -> 
            loanService.extendLoan(1L, 0));
        
        assertThrows(InvalidLoanOperationException.class, () -> 
            loanService.extendLoan(1L, 31));
        
        verify(loanRepository, never()).save(any(Loan.class));
    }
    
    @Test
    void getAllLoans_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(loanRepository.findAll(pageable)).thenReturn(loanPage);
        
        // When
        Page<Loan> result = loanService.getAllLoans(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));
        
        verify(loanRepository).findAll(pageable);
    }
    
    @Test
    void getLoanById_Success() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // When
        Optional<Loan> result = loanService.getLoanById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testLoan, result.get());
        
        verify(loanRepository).findById(1L);
    }
    
    @Test
    void getLoanById_NotFound() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        Optional<Loan> result = loanService.getLoanById(1L);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(loanRepository).findById(1L);
    }
    
    @Test
    void getActiveLoans_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(loanRepository.findByStatusOrderByDueDateAsc(LoanStatus.ACTIVE, pageable)).thenReturn(loanPage);
        
        // When
        Page<Loan> result = loanService.getActiveLoans(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));
        
        verify(loanRepository).findByStatusOrderByDueDateAsc(LoanStatus.ACTIVE, pageable);
    }
    
    @Test
    void getOverdueLoans_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(loanRepository.findOverdueLoans(pageable)).thenReturn(loanPage);
        
        // When
        Page<Loan> result = loanService.getOverdueLoans(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));
        
        verify(loanRepository).findOverdueLoans(pageable);
    }
    
    @Test
    void getLoansByBorrowerEmail_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 20);
        String email = "john.doe@example.com";
        
        when(loanRepository.findByBorrowerEmailIgnoreCase(email, pageable)).thenReturn(loanPage);
        
        // When
        Page<Loan> result = loanService.getLoansByBorrowerEmail(email, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));
        
        verify(loanRepository).findByBorrowerEmailIgnoreCase(email, pageable);
    }
    
    @Test
    void getLoansDueToday_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanRepository.findLoansDueToday()).thenReturn(loans);
        
        // When
        List<Loan> result = loanService.getLoansDueToday();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLoan, result.get(0));
        
        verify(loanRepository).findLoansDueToday();
    }
    
    @Test
    void getLoansDueWithinDays_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(loanRepository.findLoansDueWithinDays(endDate)).thenReturn(loans);
        
        // When
        List<Loan> result = loanService.getLoansDueWithinDays(7);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLoan, result.get(0));
        
        verify(loanRepository).findLoansDueWithinDays(endDate);
    }
    
    @Test
    void isBookCurrentlyLoaned_True() {
        // Given
        when(loanRepository.isBookCurrentlyLoaned(1L)).thenReturn(true);
        
        // When
        boolean result = loanService.isBookCurrentlyLoaned(1L);
        
        // Then
        assertTrue(result);
        
        verify(loanRepository).isBookCurrentlyLoaned(1L);
    }
    
    @Test
    void isBookCurrentlyLoaned_False() {
        // Given
        when(loanRepository.isBookCurrentlyLoaned(1L)).thenReturn(false);
        
        // When
        boolean result = loanService.isBookCurrentlyLoaned(1L);
        
        // Then
        assertFalse(result);
        
        verify(loanRepository).isBookCurrentlyLoaned(1L);
    }
    
    @Test
    void getActiveLoanCountByBorrower_Success() {
        // Given
        String email = "john.doe@example.com";
        when(loanRepository.countActiveLoansByBorrower(email)).thenReturn(3L);
        
        // When
        Long result = loanService.getActiveLoanCountByBorrower(email);
        
        // Then
        assertEquals(3L, result);
        
        verify(loanRepository).countActiveLoansByBorrower(email);
    }
    
    @Test
    void updateOverdueLoans_Success() {
        // Given
        Loan overdueLoan = new Loan();
        overdueLoan.setId(2L);
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        overdueLoan.setDueDate(LocalDate.now().minusDays(1)); // Overdue
        
        List<Loan> activeLoans = Arrays.asList(testLoan, overdueLoan);
        Page<Loan> loanPage = new PageImpl<>(activeLoans);
        
        when(loanRepository.findByStatus(LoanStatus.ACTIVE, Pageable.unpaged())).thenReturn(loanPage);
        when(loanRepository.save(any(Loan.class))).thenReturn(overdueLoan);
        
        // When
        loanService.updateOverdueLoans();
        
        // Then
        verify(loanRepository).findByStatus(LoanStatus.ACTIVE, Pageable.unpaged());
        verify(loanRepository).save(overdueLoan);
    }
    
    @Test
    void getLoanStatistics_Success() {
        // Given
        when(loanRepository.countByStatus(LoanStatus.ACTIVE)).thenReturn(5L);
        when(loanRepository.countByStatus(LoanStatus.OVERDUE)).thenReturn(2L);
        when(loanRepository.countByStatus(LoanStatus.RETURNED)).thenReturn(10L);
        
        // When
        LoanService.LoanStatistics result = loanService.getLoanStatistics();
        
        // Then
        assertNotNull(result);
        assertEquals(5L, result.getActiveLoans());
        assertEquals(2L, result.getOverdueLoans());
        assertEquals(10L, result.getReturnedLoans());
        assertEquals(17L, result.getTotalLoans());
        
        verify(loanRepository).countByStatus(LoanStatus.ACTIVE);
        verify(loanRepository).countByStatus(LoanStatus.OVERDUE);
        verify(loanRepository).countByStatus(LoanStatus.RETURNED);
    }
    
    @Test
    void searchLoans_Success() {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 20);
        
        String borrowerEmail = "john.doe@example.com";
        String borrowerName = "John Doe";
        LoanStatus status = LoanStatus.ACTIVE;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(loanRepository.searchLoans(borrowerEmail, borrowerName, status, startDate, endDate, pageable))
            .thenReturn(loanPage);
        
        // When
        Page<Loan> result = loanService.searchLoans(borrowerEmail, borrowerName, status, startDate, endDate, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));
        
        verify(loanRepository).searchLoans(borrowerEmail, borrowerName, status, startDate, endDate, pageable);
    }
}