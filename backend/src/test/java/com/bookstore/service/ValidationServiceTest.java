package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.exception.BusinessRuleViolationException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.exception.ValidationException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService(bookRepository, loanRepository);
    }

    @Test
    void shouldValidateBookCreationSuccessfully() {
        // Given
        Book book = new Book();
        book.setTitle("Test Book");
        book.setIsbn("9780123456786");
        book.setTotalCopies(10);
        book.setAvailableCopies(5);
        book.setPublicationYear(2023);

        when(bookRepository.findByIsbn("9780123456786")).thenReturn(Optional.empty());

        // When & Then
        validationService.validateBookCreation(book);
        // Should not throw any exception
    }

    @Test
    void shouldRejectBookCreationWithDuplicateISBN() {
        // Given
        Book book = new Book();
        book.setIsbn("9780123456786");

        Book existingBook = new Book();
        when(bookRepository.findByIsbn("9780123456786")).thenReturn(Optional.of(existingBook));

        // When & Then
        assertThatThrownBy(() -> validationService.validateBookCreation(book))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Book already exists with identifier: 9780123456786");
    }

    @Test
    void shouldRejectBookCreationWithInvalidCopyCount() {
        // Given
        Book book = new Book();
        book.setIsbn("9780123456786");
        book.setTotalCopies(5);
        book.setAvailableCopies(10);

        when(bookRepository.findByIsbn("9780123456786")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> validationService.validateBookCreation(book))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Book validation failed");
    }

    @Test
    void shouldRejectBookCreationWithFuturePublicationYear() {
        // Given
        Book book = new Book();
        book.setIsbn("9780123456786");
        book.setPublicationYear(LocalDate.now().getYear() + 5);

        when(bookRepository.findByIsbn("9780123456786")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> validationService.validateBookCreation(book))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Publication year cannot be more than one year in the future");
    }

    @Test
    void shouldValidateLoanCreationSuccessfully() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(5);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerEmail("test@example.com");
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));

        when(loanRepository.existsByBookAndBorrowerEmailAndStatus(eq(book), eq("test@example.com"), eq(LoanStatus.ACTIVE)))
                .thenReturn(false);

        // When & Then
        validationService.validateLoanCreation(loan);
        // Should not throw any exception
    }

    @Test
    void shouldRejectLoanCreationForUnavailableBook() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(0);

        Loan loan = new Loan();
        loan.setBook(book);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanCreation(loan))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Book is not available for loan");
    }

    @Test
    void shouldRejectLoanCreationForDuplicateActiveLoan() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(5);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerEmail("test@example.com");

        when(loanRepository.existsByBookAndBorrowerEmailAndStatus(eq(book), eq("test@example.com"), eq(LoanStatus.ACTIVE)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanCreation(loan))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Borrower already has an active loan for this book");
    }

    @Test
    void shouldRejectLoanCreationWithInvalidDates() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(5);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerEmail("test@example.com");
        loan.setLoanDate(LocalDate.now().plusDays(14));
        loan.setDueDate(LocalDate.now());

        when(loanRepository.existsByBookAndBorrowerEmailAndStatus(any(), any(), any()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanCreation(loan))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Due date must be after loan date");
    }

    @Test
    void shouldRejectLoanCreationWithPastLoanDate() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(5);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerEmail("test@example.com");
        loan.setLoanDate(LocalDate.now().minusDays(1));
        loan.setDueDate(LocalDate.now().plusDays(14));

        when(loanRepository.existsByBookAndBorrowerEmailAndStatus(any(), any(), any()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanCreation(loan))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Loan date cannot be in the past");
    }

    @Test
    void shouldRejectLoanCreationWithExcessiveLoanPeriod() {
        // Given
        Book book = new Book();
        book.setAvailableCopies(5);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerEmail("test@example.com");
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusMonths(7));

        when(loanRepository.existsByBookAndBorrowerEmailAndStatus(any(), any(), any()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanCreation(loan))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Loan period cannot exceed 6 months");
    }

    @Test
    void shouldValidateLoanReturnSuccessfully() {
        // Given
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setReturnDate(null);

        // When & Then
        validationService.validateLoanReturn(loan);
        // Should not throw any exception
    }

    @Test
    void shouldRejectLoanReturnForInactiveLoan() {
        // Given
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.RETURNED);

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanReturn(loan))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only active loans can be returned");
    }

    @Test
    void shouldRejectLoanReturnForAlreadyReturnedLoan() {
        // Given
        Loan loan = new Loan();
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setReturnDate(LocalDate.now());

        // When & Then
        assertThatThrownBy(() -> validationService.validateLoanReturn(loan))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Loan has already been returned");
    }

    @Test
    void shouldValidateSearchParametersSuccessfully() {
        // When & Then
        validationService.validateSearchParameters("test query", 0, 20);
        // Should not throw any exception
    }

    @Test
    void shouldRejectSearchWithShortQuery() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchParameters("a", 0, 20))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Search query must be at least 2 characters long");
    }

    @Test
    void shouldRejectSearchWithNegativePage() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchParameters("test", -1, 20))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Page number cannot be negative");
    }

    @Test
    void shouldRejectSearchWithInvalidPageSize() {
        // When & Then
        assertThatThrownBy(() -> validationService.validateSearchParameters("test", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Page size must be at least 1");

        assertThatThrownBy(() -> validationService.validateSearchParameters("test", 0, 101))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Page size cannot exceed 100");
    }

    @Test
    void shouldValidateBookDeletionSuccessfully() {
        // Given
        Book book = new Book();
        when(loanRepository.countByBookAndStatus(book, LoanStatus.ACTIVE)).thenReturn(0L);

        // When & Then
        validationService.validateBookDeletion(book);
        // Should not throw any exception
    }

    @Test
    void shouldRejectBookDeletionWithActiveLoans() {
        // Given
        Book book = new Book();
        when(loanRepository.countByBookAndStatus(book, LoanStatus.ACTIVE)).thenReturn(2L);

        // When & Then
        assertThatThrownBy(() -> validationService.validateBookDeletion(book))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot delete book with 2 active loans");
    }
}