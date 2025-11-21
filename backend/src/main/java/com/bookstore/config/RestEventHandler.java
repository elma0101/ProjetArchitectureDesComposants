package com.bookstore.config;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RepositoryEventHandler
public class RestEventHandler {

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private LoanRepository loanRepository;

    @HandleBeforeCreate
    public void handleBookBeforeCreate(Book book) {
        // Validate ISBN uniqueness
        if (book.getIsbn() != null && bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("A book with ISBN " + book.getIsbn() + " already exists");
        }
        
        // Set default values
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies() != null ? book.getTotalCopies() : 0);
        }
        
        // Validate available copies don't exceed total copies
        if (book.getTotalCopies() != null && book.getAvailableCopies() != null 
            && book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }
    }

    @HandleBeforeSave
    public void handleBookBeforeSave(Book book) {
        // Validate available copies don't exceed total copies
        if (book.getTotalCopies() != null && book.getAvailableCopies() != null 
            && book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }
        
        // Ensure available copies is not negative
        if (book.getAvailableCopies() != null && book.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }
    }

    @HandleBeforeCreate
    public void handleLoanBeforeCreate(Loan loan) {
        // Validate book availability
        if (loan.getBook() != null) {
            Book book = bookRepository.findById(loan.getBook().getId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            
            if (!book.isAvailable()) {
                throw new IllegalArgumentException("Book is not available for loan");
            }
            
            // Check if book is already loaned to prevent double booking
            boolean isAlreadyLoaned = loanRepository.isBookCurrentlyLoaned(book.getId());
            if (isAlreadyLoaned) {
                throw new IllegalArgumentException("Book is already on loan");
            }
        }
        
        // Set default loan date if not provided
        if (loan.getLoanDate() == null) {
            loan.setLoanDate(LocalDate.now());
        }
        
        // Set default due date if not provided (14 days from loan date)
        if (loan.getDueDate() == null) {
            loan.setDueDate(loan.getLoanDate().plusDays(14));
        }
        
        // Validate due date is after loan date
        if (loan.getDueDate().isBefore(loan.getLoanDate())) {
            throw new IllegalArgumentException("Due date must be after loan date");
        }
        
        // Set default status
        if (loan.getStatus() == null) {
            loan.setStatus(LoanStatus.ACTIVE);
        }
        
        // Validate borrower has not exceeded loan limit
        long activeLoanCount = loanRepository.countActiveLoansByBorrower(loan.getBorrowerEmail());
        if (activeLoanCount >= 5) { // Maximum 5 active loans per borrower
            throw new IllegalArgumentException("Borrower has reached maximum loan limit (5 active loans)");
        }
    }

    @HandleBeforeSave
    public void handleLoanBeforeSave(Loan loan) {
        // Update book availability when loan status changes
        if (loan.getBook() != null) {
            Book book = bookRepository.findById(loan.getBook().getId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            
            // If loan is being returned, increase available copies
            if (loan.getStatus() == LoanStatus.RETURNED && loan.getReturnDate() == null) {
                loan.setReturnDate(LocalDate.now());
                if (book.getAvailableCopies() != null && book.getTotalCopies() != null 
                    && book.getAvailableCopies() < book.getTotalCopies()) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    bookRepository.save(book);
                }
            }
            
            // Check for overdue status
            if (loan.getStatus() == LoanStatus.ACTIVE && loan.getDueDate().isBefore(LocalDate.now())) {
                loan.setStatus(LoanStatus.OVERDUE);
            }
        }
        
        // Validate return date logic
        if (loan.getReturnDate() != null && loan.getReturnDate().isBefore(loan.getLoanDate())) {
            throw new IllegalArgumentException("Return date cannot be before loan date");
        }
    }
}