package com.bookstore.service;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.exception.BusinessRuleViolationException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.exception.ValidationException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling complex business rule validations
 */
@Service
public class ValidationService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public ValidationService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Validates book creation business rules
     */
    public void validateBookCreation(Book book) {
        Map<String, String> errors = new HashMap<>();

        // Check for duplicate ISBN
        if (book.getIsbn() != null) {
            Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
            if (existingBook.isPresent()) {
                throw new DuplicateResourceException("Book", book.getIsbn());
            }
        }

        // Validate copy counts
        if (book.getTotalCopies() != null && book.getAvailableCopies() != null) {
            if (book.getAvailableCopies() > book.getTotalCopies()) {
                errors.put("availableCopies", "Available copies cannot exceed total copies");
            }
        }

        // Validate publication year
        if (book.getPublicationYear() != null) {
            int currentYear = LocalDate.now().getYear();
            if (book.getPublicationYear() > currentYear + 1) {
                errors.put("publicationYear", "Publication year cannot be more than one year in the future");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Book validation failed", errors);
        }
    }

    /**
     * Validates book update business rules
     */
    public void validateBookUpdate(Book book, Book existingBook) {
        Map<String, String> errors = new HashMap<>();

        // Check if ISBN is being changed to an existing one
        if (book.getIsbn() != null && !book.getIsbn().equals(existingBook.getIsbn())) {
            Optional<Book> bookWithNewIsbn = bookRepository.findByIsbn(book.getIsbn());
            if (bookWithNewIsbn.isPresent()) {
                throw new DuplicateResourceException("Book", book.getIsbn());
            }
        }

        // Validate that available copies don't exceed what's physically possible
        if (book.getAvailableCopies() != null && book.getTotalCopies() != null) {
            if (book.getAvailableCopies() > book.getTotalCopies()) {
                errors.put("availableCopies", "Available copies cannot exceed total copies");
            }

            // Check if reducing available copies would conflict with active loans
            long activeLoans = loanRepository.countByBookAndStatus(existingBook, LoanStatus.ACTIVE);
            long maxAvailableCopies = book.getTotalCopies() - activeLoans;
            
            if (book.getAvailableCopies() > maxAvailableCopies) {
                errors.put("availableCopies", 
                    String.format("Cannot set available copies to %d. Maximum allowed is %d due to %d active loans", 
                        book.getAvailableCopies(), maxAvailableCopies, activeLoans));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Book update validation failed", errors);
        }
    }

    /**
     * Validates loan creation business rules
     */
    public void validateLoanCreation(Loan loan) {
        Map<String, String> errors = new HashMap<>();

        // Check if book is available
        if (loan.getBook() != null) {
            if (!loan.getBook().isAvailable()) {
                throw new BusinessRuleViolationException("BOOK_NOT_AVAILABLE", 
                    "Book is not available for loan");
            }

            // Check if borrower already has an active loan for this book
            boolean hasActiveLoan = loanRepository.existsByBookAndBorrowerEmailAndStatus(
                loan.getBook(), loan.getBorrowerEmail(), LoanStatus.ACTIVE);
            
            if (hasActiveLoan) {
                throw new BusinessRuleViolationException("DUPLICATE_ACTIVE_LOAN", 
                    "Borrower already has an active loan for this book");
            }
        }

        // Validate loan dates
        if (loan.getLoanDate() != null && loan.getDueDate() != null) {
            if (loan.getLoanDate().isAfter(loan.getDueDate())) {
                errors.put("dueDate", "Due date must be after loan date");
            }

            // Loan date cannot be in the past (except for today)
            if (loan.getLoanDate().isBefore(LocalDate.now())) {
                errors.put("loanDate", "Loan date cannot be in the past");
            }

            // Due date should be reasonable (not more than 6 months)
            if (loan.getDueDate().isAfter(loan.getLoanDate().plusMonths(6))) {
                errors.put("dueDate", "Loan period cannot exceed 6 months");
            }
        }

        // Validate borrower email format (additional to @Email annotation)
        if (loan.getBorrowerEmail() != null) {
            String email = loan.getBorrowerEmail().toLowerCase();
            if (email.contains("test") || email.contains("example")) {
                errors.put("borrowerEmail", "Test or example emails are not allowed");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Loan creation validation failed", errors);
        }
    }

    /**
     * Validates loan return business rules
     */
    public void validateLoanReturn(Loan loan) {
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleViolationException("INVALID_LOAN_STATUS", 
                "Only active loans can be returned");
        }

        if (loan.getReturnDate() != null) {
            throw new BusinessRuleViolationException("LOAN_ALREADY_RETURNED", 
                "Loan has already been returned");
        }
    }

    /**
     * Validates that a book can be deleted
     */
    public void validateBookDeletion(Book book) {
        long activeLoans = loanRepository.countByBookAndStatus(book, LoanStatus.ACTIVE);
        if (activeLoans > 0) {
            throw new BusinessRuleViolationException("BOOK_HAS_ACTIVE_LOANS", 
                String.format("Cannot delete book with %d active loans", activeLoans));
        }
    }

    /**
     * Validates search parameters
     */
    public void validateSearchParameters(String query, Integer page, Integer size) {
        Map<String, String> errors = new HashMap<>();

        if (query != null && query.trim().length() < 2) {
            errors.put("query", "Search query must be at least 2 characters long");
        }

        if (page != null && page < 0) {
            errors.put("page", "Page number cannot be negative");
        }

        if (size != null) {
            if (size < 1) {
                errors.put("size", "Page size must be at least 1");
            } else if (size > 100) {
                errors.put("size", "Page size cannot exceed 100");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Search parameter validation failed", errors);
        }
    }

    /**
     * Validates recommendation parameters
     */
    public void validateRecommendationRequest(String userId, Integer limit) {
        Map<String, String> errors = new HashMap<>();

        if (userId != null && userId.trim().isEmpty()) {
            errors.put("userId", "User ID cannot be empty");
        }

        if (limit != null) {
            if (limit < 1) {
                errors.put("limit", "Limit must be at least 1");
            } else if (limit > 50) {
                errors.put("limit", "Limit cannot exceed 50");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Recommendation request validation failed", errors);
        }
    }
}