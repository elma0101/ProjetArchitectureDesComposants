package com.bookstore.loanmanagement.saga;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.LoanNotFoundException;
import com.bookstore.loanmanagement.repository.LoanRepository;
import com.bookstore.loanmanagement.service.LoanEventPublisher;
import com.bookstore.loanmanagement.service.LoanTrackingService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanReturnSagaOrchestratorTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private BookCatalogClient bookCatalogClient;
    
    @Mock
    private LoanTrackingService loanTrackingService;
    
    @Mock
    private LoanEventPublisher loanEventPublisher;
    
    @InjectMocks
    private LoanReturnSagaOrchestrator sagaOrchestrator;
    
    private Loan loan;
    
    @BeforeEach
    void setUp() {
        loan = new Loan();
        loan.setId(1L);
        loan.setUserId(1L);
        loan.setBookId(100L);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setLoanDate(LocalDate.now().minusDays(7));
        loan.setDueDate(LocalDate.now().plusDays(7));
    }
    
    @Test
    void executeLoanReturnSaga_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).returnBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanReturned(anyLong(), anyBoolean());
        doNothing().when(loanEventPublisher).publishLoanReturned(any());
        
        // Act
        Loan result = sagaOrchestrator.executeLoanReturnSaga(1L, "Returned in good condition");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        
        verify(loanRepository).save(any(Loan.class));
        verify(bookCatalogClient).returnBook(100L);
        verify(loanTrackingService).recordLoanReturned(eq(1L), anyBoolean());
        verify(loanEventPublisher).publishLoanReturned(any());
    }
    
    @Test
    void executeLoanReturnSaga_LoanNotFound_ThrowsException() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanReturnSaga(1L, "Notes"))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessageContaining("Loan not found");
        
        verify(loanRepository, never()).save(any());
        verify(bookCatalogClient, never()).returnBook(anyLong());
    }
    
    @Test
    void executeLoanReturnSaga_BookReturnFails_CompensatesLoan() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        
        Request request = Request.create(Request.HttpMethod.POST, "/api/books/100/return",
                new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.InternalServerError("Server error", request, null, null))
                .when(bookCatalogClient).returnBook(anyLong());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanReturnSaga(1L, "Notes"))
                .isInstanceOf(BookNotAvailableException.class);
        
        // Verify compensation
        verify(loanRepository, atLeast(2)).save(any(Loan.class));
        verify(loanTrackingService).recordLoanCancelled(eq(1L), anyString());
    }
    
    @Test
    void executeLoanReturnSaga_OverdueLoan_MarksAsOverdue() {
        // Arrange
        loan.setDueDate(LocalDate.now().minusDays(5)); // Overdue
        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).returnBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanReturned(anyLong(), anyBoolean());
        doNothing().when(loanEventPublisher).publishLoanReturned(any());
        
        // Act
        Loan result = sagaOrchestrator.executeLoanReturnSaga(1L, "Returned late");
        
        // Assert
        assertThat(result).isNotNull();
        verify(loanTrackingService).recordLoanReturned(eq(1L), eq(true)); // wasOverdue = true
    }
    
    @Test
    void compensateBookReturn_CallsBorrowBook() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        
        // First call succeeds (marks as returned), second call fails (return to catalog)
        doNothing().doThrow(new RuntimeException("Catalog error"))
                .when(bookCatalogClient).returnBook(anyLong());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanReturnSaga(1L, "Notes"))
                .isInstanceOf(BookNotAvailableException.class);
        
        // Verify compensation attempted to borrow the book again
        verify(bookCatalogClient).borrowBook(100L);
    }
}
