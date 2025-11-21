package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanNotificationServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private EmailNotificationService emailNotificationService;
    
    @Mock
    private LoanTrackingService loanTrackingService;
    
    @InjectMocks
    private LoanNotificationService loanNotificationService;
    
    private Loan testLoan;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setBook(testBook);
        testLoan.setBorrowerName("John Doe");
        testLoan.setBorrowerEmail("john.doe@email.com");
        testLoan.setLoanDate(LocalDate.now().minusDays(10));
        testLoan.setDueDate(LocalDate.now().minusDays(1)); // Overdue
        testLoan.setStatus(LoanStatus.ACTIVE);
    }
    
    @Test
    void testProcessOverdueLoans() {
        // Given
        List<Loan> activeLoans = Arrays.asList(testLoan);
        Page<Loan> activeLoanPage = new PageImpl<>(activeLoans);
        
        when(loanRepository.findByStatus(eq(LoanStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(activeLoanPage);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        // Remove this line as it's not needed for this specific test
        
        // When
        loanNotificationService.processOverdueLoans();
        
        // Then
        verify(loanRepository).save(testLoan);
        verify(emailNotificationService).sendNotification(
            eq("john.doe@email.com"),
            eq("John Doe"),
            eq("Overdue Book Return Notice"),
            anyString()
        );
        verify(loanTrackingService).recordStatusChange(1L, LoanStatus.ACTIVE, LoanStatus.OVERDUE);
        verify(loanTrackingService).recordNotificationSent(1L, "OVERDUE_NOTIFICATION");
    }
    
    @Test
    void testSendDueReminders() {
        // Given
        testLoan.setDueDate(LocalDate.now().plusDays(2)); // Due in 2 days
        testLoan.setStatus(LoanStatus.ACTIVE);
        
        List<Loan> loansDueSoon = Arrays.asList(testLoan);
        
        when(loanRepository.findLoansDueWithinDays(any(LocalDate.class)))
            .thenReturn(loansDueSoon);
        when(loanTrackingService.wasNotificationSentToday(anyLong(), anyString()))
            .thenReturn(false);
        
        // When
        loanNotificationService.sendDueReminders();
        
        // Then
        verify(emailNotificationService).sendNotification(
            eq("john.doe@email.com"),
            eq("John Doe"),
            eq("Book Return Reminder"),
            anyString()
        );
        verify(loanTrackingService).recordNotificationSent(1L, "DUE_REMINDER");
    }
    
    @Test
    void testSendDueReminders_SkipIfAlreadySent() {
        // Given
        testLoan.setDueDate(LocalDate.now().plusDays(2));
        testLoan.setStatus(LoanStatus.ACTIVE);
        
        List<Loan> loansDueSoon = Arrays.asList(testLoan);
        
        when(loanRepository.findLoansDueWithinDays(any(LocalDate.class)))
            .thenReturn(loansDueSoon);
        when(loanTrackingService.wasNotificationSentToday(1L, "DUE_REMINDER"))
            .thenReturn(true);
        
        // When
        loanNotificationService.sendDueReminders();
        
        // Then
        verify(emailNotificationService, never()).sendNotification(anyString(), anyString(), anyString(), anyString());
        verify(loanTrackingService, never()).recordNotificationSent(anyLong(), anyString());
    }
    
    @Test
    void testSendLoanConfirmation() {
        // Given
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setDueDate(LocalDate.now().plusDays(14));
        
        // When
        loanNotificationService.sendLoanConfirmation(testLoan);
        
        // Then
        verify(emailNotificationService).sendNotification(
            eq("john.doe@email.com"),
            eq("John Doe"),
            eq("Book Loan Confirmation"),
            anyString()
        );
        verify(loanTrackingService).recordNotificationSent(1L, "LOAN_CONFIRMATION");
    }
    
    @Test
    void testSendReturnConfirmation() {
        // Given
        testLoan.setStatus(LoanStatus.RETURNED);
        testLoan.setReturnDate(LocalDate.now());
        
        // When
        loanNotificationService.sendReturnConfirmation(testLoan);
        
        // Then
        verify(emailNotificationService).sendNotification(
            eq("john.doe@email.com"),
            eq("John Doe"),
            eq("Book Return Confirmation"),
            anyString()
        );
        verify(loanTrackingService).recordNotificationSent(1L, "RETURN_CONFIRMATION");
    }
    
    @Test
    void testProcessOverdueLoans_NoOverdueLoans() {
        // Given
        when(loanRepository.findByStatus(eq(LoanStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        
        // When
        loanNotificationService.processOverdueLoans();
        
        // Then
        verify(emailNotificationService, never()).sendNotification(anyString(), anyString(), anyString(), anyString());
        verify(loanTrackingService, never()).recordNotificationSent(anyLong(), anyString());
    }
    
    @Test
    void testTriggerOverdueProcessing() {
        // Given
        List<Loan> activeLoans = Arrays.asList(testLoan);
        Page<Loan> activeLoanPage = new PageImpl<>(activeLoans);
        
        when(loanRepository.findByStatus(eq(LoanStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(activeLoanPage);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        
        // When
        loanNotificationService.triggerOverdueProcessing();
        
        // Then
        verify(loanRepository).save(testLoan);
        verify(emailNotificationService).sendNotification(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testTriggerDueReminders() {
        // Given
        testLoan.setDueDate(LocalDate.now().plusDays(2));
        testLoan.setStatus(LoanStatus.ACTIVE);
        
        List<Loan> loansDueSoon = Arrays.asList(testLoan);
        
        when(loanRepository.findLoansDueWithinDays(any(LocalDate.class)))
            .thenReturn(loansDueSoon);
        when(loanTrackingService.wasNotificationSentToday(anyLong(), anyString()))
            .thenReturn(false);
        
        // When
        loanNotificationService.triggerDueReminders();
        
        // Then
        verify(emailNotificationService).sendNotification(anyString(), anyString(), anyString(), anyString());
        verify(loanTrackingService).recordNotificationSent(1L, "DUE_REMINDER");
    }
}