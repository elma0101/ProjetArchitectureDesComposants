package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.client.BookCatalogClient;
import com.bookstore.loanmanagement.dto.BookResponse;
import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.dto.LoanResponse;
import com.bookstore.loanmanagement.dto.LoanStatistics;
import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.exception.BookNotAvailableException;
import com.bookstore.loanmanagement.exception.BookNotFoundException;
import com.bookstore.loanmanagement.exception.InvalidLoanOperationException;
import com.bookstore.loanmanagement.exception.LoanNotFoundException;
import com.bookstore.loanmanagement.repository.LoanRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCatalogClient bookCatalogClient;

    @Mock
    private LoanTrackingService loanTrackingService;

    @InjectMocks
    private LoanService loanService;

    private LoanRequest loanRequest;
    private BookResponse bookResponse;
    private Loan loan;

    @BeforeEach
    void setUp() {
        loanRequest = new LoanRequest(1L, 1L, "Test loan");
        
        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Test Book");
        bookResponse.setIsbn("1234567890");
        bookResponse.setTotalCopies(5);
        bookResponse.setAvailableCopies(3);
        
        loan = new Loan();
        loan.setId(1L);
        loan.setUserId(1L);
        loan.setBookId(1L);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
    }

    @Test
    void createLoan_Success() {
        // Arrange
        when(bookCatalogClient.checkAvailability(anyLong())).thenReturn(bookResponse);
        when(loanRepository.findByUserIdAndStatus(anyLong(), any())).thenReturn(List.of());
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).borrowBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanCreated(anyLong(), anyLong(), anyLong());

        // Act
        LoanResponse response = loanService.createLoan(loanRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getBookId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        
        verify(bookCatalogClient).checkAvailability(1L);
        verify(bookCatalogClient).borrowBook(1L);
        verify(loanRepository).save(any(Loan.class));
        verify(loanTrackingService).recordLoanCreated(anyLong(), eq(1L), eq(1L));
    }

    @Test
    void createLoan_BookNotFound() {
        // Arrange
        when(bookCatalogClient.checkAvailability(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found");
        
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_BookNotAvailable() {
        // Arrange
        bookResponse.setAvailableCopies(0);
        when(bookCatalogClient.checkAvailability(anyLong())).thenReturn(bookResponse);

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(BookNotAvailableException.class)
                .hasMessageContaining("not available for loan");
        
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_UserAlreadyHasActiveLoan() {
        // Arrange
        Loan existingLoan = new Loan();
        existingLoan.setBookId(1L);
        existingLoan.setUserId(1L);
        existingLoan.setStatus(LoanStatus.ACTIVE);
        
        when(bookCatalogClient.checkAvailability(anyLong())).thenReturn(bookResponse);
        when(loanRepository.findByUserIdAndStatus(anyLong(), any()))
                .thenReturn(List.of(existingLoan));

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(InvalidLoanOperationException.class)
                .hasMessageContaining("already has an active loan");
        
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoan_Success() {
        // Arrange
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(bookCatalogClient).returnBook(anyLong());
        doNothing().when(loanTrackingService).recordLoanReturned(anyLong(), anyBoolean());

        // Act
        LoanResponse response = loanService.returnLoan(1L, "Returned in good condition");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(LoanStatus.RETURNED);
        
        verify(loanRepository).findById(1L);
        verify(bookCatalogClient).returnBook(1L);
        verify(loanTrackingService).recordLoanReturned(anyLong(), anyBoolean());
    }

    @Test
    void returnLoan_LoanNotFound() {
        // Arrange
        when(loanRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.returnLoan(1L, null))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessageContaining("Loan not found");
    }

    @Test
    void returnLoan_LoanAlreadyReturned() {
        // Arrange
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        // Act & Assert
        assertThatThrownBy(() -> loanService.returnLoan(1L, null))
                .isInstanceOf(InvalidLoanOperationException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void extendLoan_Success() {
        // Arrange
        LocalDate originalDueDate = loan.getDueDate();
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        doNothing().when(loanTrackingService).recordLoanExtension(anyLong(), any(), any(), anyInt());

        // Act
        LoanResponse response = loanService.extendLoan(1L, 7);

        // Assert
        assertThat(response).isNotNull();
        verify(loanTrackingService).recordLoanExtension(anyLong(), eq(originalDueDate), any(), eq(7));
    }

    @Test
    void extendLoan_InvalidDays() {
        // Arrange
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        // Act & Assert
        assertThatThrownBy(() -> loanService.extendLoan(1L, 0))
                .isInstanceOf(InvalidLoanOperationException.class)
                .hasMessageContaining("Extension days must be");
        
        assertThatThrownBy(() -> loanService.extendLoan(1L, 31))
                .isInstanceOf(InvalidLoanOperationException.class)
                .hasMessageContaining("Extension days must be");
    }

    @Test
    void getLoanById_Success() {
        // Arrange
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        // Act
        LoanResponse response = loanService.getLoanById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getActiveLoansForUser_Success() {
        // Arrange
        when(loanRepository.findByUserIdAndStatus(anyLong(), any()))
                .thenReturn(Arrays.asList(loan));

        // Act
        List<LoanResponse> responses = loanService.getActiveLoansForUser(1L);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void updateOverdueLoans_Success() {
        // Arrange
        Loan overdueLoan = new Loan();
        overdueLoan.setId(2L);
        overdueLoan.setUserId(1L);
        overdueLoan.setBookId(1L);
        overdueLoan.setLoanDate(LocalDate.now().minusDays(20));
        overdueLoan.setDueDate(LocalDate.now().minusDays(5));
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        
        when(loanRepository.findByStatus(LoanStatus.ACTIVE))
                .thenReturn(Arrays.asList(overdueLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(overdueLoan);
        doNothing().when(loanTrackingService).recordLoanOverdue(anyLong());

        // Act
        int count = loanService.updateOverdueLoans();

        // Assert
        assertThat(count).isEqualTo(1);
        verify(loanTrackingService).recordLoanOverdue(2L);
    }

    @Test
    void getLoanStatistics_Success() {
        // Arrange
        when(loanRepository.findByStatus(LoanStatus.ACTIVE)).thenReturn(Arrays.asList(loan));
        when(loanRepository.findOverdueLoans(any())).thenReturn(List.of());
        when(loanRepository.findByStatus(LoanStatus.RETURNED)).thenReturn(List.of());

        // Act
        LoanStatistics stats = loanService.getLoanStatistics();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getActiveLoans()).isEqualTo(1L);
        assertThat(stats.getOverdueLoans()).isEqualTo(0L);
        assertThat(stats.getReturnedLoans()).isEqualTo(0L);
        assertThat(stats.getTotalLoans()).isEqualTo(1L);
    }
}
