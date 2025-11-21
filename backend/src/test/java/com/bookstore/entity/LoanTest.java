package com.bookstore.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {
    
    private Validator validator;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        testBook = new Book("Test Title", "978-0-452-28423-4");
        testBook.setId(1L);
    }
    
    @Test
    void testValidLoan() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        loan.setBorrowerId("USER001");
        loan.setNotes("Test loan");
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertTrue(violations.isEmpty(), "Valid loan should have no validation errors");
    }
    
    @Test
    void testLoanWithNullBook() {
        Loan loan = new Loan(null, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with null book should have validation errors");
        
        boolean bookViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("book"));
        assertTrue(bookViolationFound, "Should have book validation error");
    }
    
    @Test
    void testLoanWithBlankBorrowerName() {
        Loan loan = new Loan(testBook, "", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with blank borrower name should have validation errors");
        
        boolean nameViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("borrowerName"));
        assertTrue(nameViolationFound, "Should have borrower name validation error");
    }
    
    @Test
    void testLoanWithInvalidEmail() {
        Loan loan = new Loan(testBook, "John Doe", "invalid-email", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with invalid email should have validation errors");
        
        boolean emailViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("borrowerEmail"));
        assertTrue(emailViolationFound, "Should have email validation error");
    }
    
    @Test
    void testLoanWithNullDates() {
        Loan loan = new Loan();
        loan.setBook(testBook);
        loan.setBorrowerName("John Doe");
        loan.setBorrowerEmail("john.doe@email.com");
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with null dates should have validation errors");
        
        long dateViolations = violations.stream()
            .filter(v -> v.getPropertyPath().toString().equals("loanDate") || 
                        v.getPropertyPath().toString().equals("dueDate"))
            .count();
        assertEquals(2, dateViolations, "Should have both loan date and due date validation errors");
    }
    
    @Test
    void testLoanWithValidDueDate() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().minusDays(1)); // Past due date should be allowed
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        // Should not have due date validation errors since we removed @Future constraint
        boolean dueDateViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("dueDate"));
        assertFalse(dueDateViolationFound, "Should not have due date validation error for past dates");
    }
    
    @Test
    void testLoanWithTooLongBorrowerName() {
        String longName = "A".repeat(201); // Exceeds 200 character limit
        Loan loan = new Loan(testBook, longName, "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with borrower name too long should have validation errors");
    }
    
    @Test
    void testLoanWithTooLongEmail() {
        String longEmail = "a".repeat(250) + "@email.com"; // Exceeds 255 character limit
        Loan loan = new Loan(testBook, "John Doe", longEmail, 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with email too long should have validation errors");
    }
    
    @Test
    void testLoanWithTooLongNotes() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        String longNotes = "A".repeat(501); // Exceeds 500 character limit
        loan.setNotes(longNotes);
        
        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertFalse(violations.isEmpty(), "Loan with notes too long should have validation errors");
    }
    
    @Test
    void testLoanIsOverdue() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        loan.setStatus(LoanStatus.ACTIVE);
        
        assertTrue(loan.isOverdue(), "Active loan past due date should be overdue");
        
        loan.setStatus(LoanStatus.RETURNED);
        assertFalse(loan.isOverdue(), "Returned loan should not be overdue");
        
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueDate(LocalDate.now().plusDays(5));
        assertFalse(loan.isOverdue(), "Active loan before due date should not be overdue");
    }
    
    @Test
    void testLoanStatusMethods() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        
        assertTrue(loan.isActive(), "New loan should be active");
        assertFalse(loan.isReturned(), "New loan should not be returned");
        
        loan.markAsReturned();
        
        assertFalse(loan.isActive(), "Returned loan should not be active");
        assertTrue(loan.isReturned(), "Marked loan should be returned");
        assertEquals(LocalDate.now(), loan.getReturnDate(), "Return date should be set to today");
        
        loan.setStatus(LoanStatus.ACTIVE);
        loan.markAsOverdue();
        assertEquals(LoanStatus.OVERDUE, loan.getStatus(), "Active loan should be marked as overdue");
    }
    
    @Test
    void testLoanGetDaysOverdue() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        loan.setStatus(LoanStatus.ACTIVE);
        
        assertEquals(5, loan.getDaysOverdue(), "Should calculate correct days overdue");
        
        loan.setStatus(LoanStatus.RETURNED);
        assertEquals(0, loan.getDaysOverdue(), "Returned loan should have 0 days overdue");
        
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueDate(LocalDate.now().plusDays(5));
        assertEquals(0, loan.getDaysOverdue(), "Future due date should have 0 days overdue");
    }
    
    @Test
    void testLoanEqualsAndHashCode() {
        Loan loan1 = new Loan(testBook, "John Doe", "john.doe@email.com", 
                             LocalDate.now(), LocalDate.now().plusDays(14));
        loan1.setId(1L);
        
        Loan loan2 = new Loan(testBook, "Jane Smith", "jane.smith@email.com", 
                             LocalDate.now(), LocalDate.now().plusDays(7));
        loan2.setId(1L);
        
        Loan loan3 = new Loan(testBook, "John Doe", "john.doe@email.com", 
                             LocalDate.now(), LocalDate.now().plusDays(14));
        loan3.setId(2L);
        
        assertEquals(loan1, loan2, "Loans with same ID should be equal");
        assertNotEquals(loan1, loan3, "Loans with different ID should not be equal");
        
        assertEquals(loan1.hashCode(), loan2.hashCode(), "Loans with same ID should have same hash code");
    }
    
    @Test
    void testLoanToString() {
        Loan loan = new Loan(testBook, "John Doe", "john.doe@email.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        loan.setId(1L);
        
        String toString = loan.toString();
        assertTrue(toString.contains("John Doe"), "toString should contain borrower name");
        assertTrue(toString.contains(loan.getStatus().toString()), "toString should contain status");
    }
}