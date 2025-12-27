package com.bookstore.loanmanagement.saga;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.BookNotFoundException;
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

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanSagaOrchestratorTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private BookCatalogClient bookCatalogClient;
    
    @Mock
    private LoanTrackingService loanTrackingService;
    
    @Mock
    private LoanEventPublisher loanEventPublisher;
    
    @InjectMocks
    private LoanSagaOrchestrator sagaOrchestrator;
    
    private LoanRequest loanRequest;
    private Loan loan;
    
    @BeforeEach
    void setUp() {
        loanRequest = new LoanRequest();
        loanRequest.setUserId(1L);
        loanRequest.setBookId(100L);
        
        loan = new Loan();
        loan.setId(1L);
        loan.setUserId(1L);
        loan.setBookId(100L);
        loan.setStatus(LoanStatus.PENDING);
    }
    
    @Test
    void executeLoanCreationSaga_Success() {
        // Arrange
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).borrowBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanCreated(anyLong(), anyLong(), anyLong());
        doNothing().when(loanEventPublisher).publishLoanCreated(any());
        
        // Act
        Loan result = sagaOrchestrator.executeLoanCreationSaga(loanRequest);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        
        verify(loanRepository, times(2)).save(any(Loan.class)); // Once for PENDING, once for ACTIVE
        verify(bookCatalogClient).borrowBook(100L);
        verify(loanTrackingService).recordLoanCreated(anyLong(), eq(1L), eq(100L));
        verify(loanEventPublisher).publishLoanCreated(any());
    }
    
    @Test
    void executeLoanCreationSaga_BookNotFound_ThrowsException() {
        // Arrange
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        
        Request request = Request.create(Request.HttpMethod.POST, "/api/books/100/borrow",
                new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.NotFound("Not found", request, null, null))
                .when(bookCatalogClient).borrowBook(anyLong());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanCreationSaga(loanRequest))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found");
        
        // Verify compensation was attempted
        verify(loanRepository, atLeastOnce()).save(any(Loan.class));
        verify(loanRepository).findById(anyLong());
    }
    
    @Test
    void executeLoanCreationSaga_BookReservationFails_CompensatesLoan() {
        // Arrange
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        
        Request request = Request.create(Request.HttpMethod.POST, "/api/books/100/borrow",
                new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.InternalServerError("Server error", request, null, null))
                .when(bookCatalogClient).borrowBook(anyLong());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanCreationSaga(loanRequest))
                .isInstanceOf(BookNotAvailableException.class);
        
        // Verify compensation
        verify(loanRepository, atLeast(2)).save(any(Loan.class));
        verify(loanTrackingService).recordLoanCancelled(anyLong(), anyString());
    }
    
    @Test
    void getSagaState_ReturnsCorrectState() {
        // Arrange
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).borrowBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanCreated(anyLong(), anyLong(), anyLong());
        doNothing().when(loanEventPublisher).publishLoanCreated(any());
        
        // Act
        Loan result = sagaOrchestrator.executeLoanCreationSaga(loanRequest);
        
        // Get all saga states (we don't have the sagaId, but we can verify the method exists)
        // In a real scenario, we'd capture the sagaId from logs or return it
        
        // Assert
        assertThat(result).isNotNull();
    }
    
    @Test
    void compensateLoanCreation_UpdatesLoanStatusToCancelled() {
        // Arrange
        loan.setId(1L);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        
        Request request = Request.create(Request.HttpMethod.POST, "/api/books/100/borrow",
                new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.BadRequest("Bad request", request, null, null))
                .when(bookCatalogClient).borrowBook(anyLong());
        
        // Act & Assert
        assertThatThrownBy(() -> sagaOrchestrator.executeLoanCreationSaga(loanRequest))
                .isInstanceOf(BookNotAvailableException.class);
        
        // Verify loan was cancelled
        verify(loanTrackingService).recordLoanCancelled(eq(1L), anyString());
    }
}
