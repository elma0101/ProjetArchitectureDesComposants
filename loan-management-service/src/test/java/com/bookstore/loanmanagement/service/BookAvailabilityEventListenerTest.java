package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.event.BookAvailabilityChangedEvent;
import com.bookstore.loanmanagement.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookAvailabilityEventListenerTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @InjectMocks
    private BookAvailabilityEventListener eventListener;
    
    private BookAvailabilityChangedEvent event;
    
    @BeforeEach
    void setUp() {
        event = new BookAvailabilityChangedEvent();
        event.setEventId("event-123");
        event.setCorrelationId("corr-456");
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("BOOK_AVAILABILITY_CHANGED");
        event.setBookId(100L);
        event.setIsbn("978-0-123456-78-9");
        event.setTitle("Test Book");
        event.setTotalCopies(5);
    }
    
    @Test
    void handleBookAvailabilityChanged_BookBorrowed_LogsCorrectly() {
        // Arrange
        event.setPreviousAvailableCopies(3);
        event.setCurrentAvailableCopies(2);
        event.setChangeReason("BORROWED");
        
        Loan loan1 = new Loan();
        loan1.setId(1L);
        loan1.setBookId(100L);
        loan1.setStatus(LoanStatus.ACTIVE);
        
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setBookId(100L);
        loan2.setStatus(LoanStatus.ACTIVE);
        
        Loan loan3 = new Loan();
        loan3.setId(3L);
        loan3.setBookId(100L);
        loan3.setStatus(LoanStatus.ACTIVE);
        
        when(loanRepository.findByBookIdAndStatus(100L, LoanStatus.ACTIVE))
                .thenReturn(Arrays.asList(loan1, loan2, loan3));
        
        // Act
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
    }
    
    @Test
    void handleBookAvailabilityChanged_BookReturned_LogsCorrectly() {
        // Arrange
        event.setPreviousAvailableCopies(2);
        event.setCurrentAvailableCopies(3);
        event.setChangeReason("RETURNED");
        
        Loan loan1 = new Loan();
        loan1.setId(1L);
        loan1.setBookId(100L);
        loan1.setStatus(LoanStatus.ACTIVE);
        
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setBookId(100L);
        loan2.setStatus(LoanStatus.ACTIVE);
        
        when(loanRepository.findByBookIdAndStatus(100L, LoanStatus.ACTIVE))
                .thenReturn(Arrays.asList(loan1, loan2));
        
        // Act
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
    }
    
    @Test
    void handleBookAvailabilityChanged_BookUnavailable_LogsWarning() {
        // Arrange
        event.setPreviousAvailableCopies(1);
        event.setCurrentAvailableCopies(0);
        event.setChangeReason("BORROWED");
        
        when(loanRepository.findByBookIdAndStatus(100L, LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        
        // Act
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
    }
    
    @Test
    void handleBookAvailabilityChanged_BookAvailableAgain_LogsInfo() {
        // Arrange
        event.setPreviousAvailableCopies(0);
        event.setCurrentAvailableCopies(1);
        event.setChangeReason("RETURNED");
        
        when(loanRepository.findByBookIdAndStatus(100L, LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        
        // Act
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
    }
    
    @Test
    void handleBookAvailabilityChanged_InconsistencyDetected_LogsWarning() {
        // Arrange
        event.setPreviousAvailableCopies(3);
        event.setCurrentAvailableCopies(2);
        event.setChangeReason("BORROWED");
        event.setTotalCopies(5);
        
        // Only 1 active loan, but expected 3 (5 total - 2 available)
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setBookId(100L);
        loan.setStatus(LoanStatus.ACTIVE);
        
        when(loanRepository.findByBookIdAndStatus(100L, LoanStatus.ACTIVE))
                .thenReturn(Collections.singletonList(loan));
        
        // Act
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
        // In real scenario, this would log a warning about inconsistency
    }
    
    @Test
    void handleBookAvailabilityChanged_ExceptionThrown_DoesNotPropagate() {
        // Arrange
        event.setPreviousAvailableCopies(3);
        event.setCurrentAvailableCopies(2);
        event.setChangeReason("BORROWED");
        
        when(loanRepository.findByBookIdAndStatus(any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act - should not throw exception
        eventListener.handleBookAvailabilityChanged(event);
        
        // Assert
        verify(loanRepository).findByBookIdAndStatus(100L, LoanStatus.ACTIVE);
    }
}
