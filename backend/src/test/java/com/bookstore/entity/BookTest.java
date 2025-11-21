package com.bookstore.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidBook() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        book.setDescription("A test book description");
        book.setPublicationYear(2023);
        book.setGenre("Fiction");
        book.setAvailableCopies(5);
        book.setTotalCopies(10);
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertTrue(violations.isEmpty(), "Valid book should have no validation errors");
    }
    
    @Test
    void testBookWithBlankTitle() {
        Book book = new Book("", "978-0-452-28423-4");
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with blank title should have validation errors");
        
        boolean titleViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        assertTrue(titleViolationFound, "Should have title validation error");
    }
    
    @Test
    void testBookWithNullTitle() {
        Book book = new Book();
        book.setIsbn("978-0-452-28423-4");
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with null title should have validation errors");
    }
    
    @Test
    void testBookWithInvalidIsbn() {
        Book book = new Book("Test Title", "invalid-isbn");
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with invalid ISBN should have validation errors");
        
        boolean isbnViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("isbn"));
        assertTrue(isbnViolationFound, "Should have ISBN validation error");
    }
    
    @Test
    void testBookWithNegativeAvailableCopies() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        book.setAvailableCopies(-1);
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with negative available copies should have validation errors");
    }
    
    @Test
    void testBookWithInvalidPublicationYear() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        book.setPublicationYear(999); // Too early
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with invalid publication year should have validation errors");
        
        book.setPublicationYear(2101); // Too late
        violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with future publication year should have validation errors");
    }
    
    @Test
    void testBookTitleTooLong() {
        String longTitle = "A".repeat(256); // Exceeds 255 character limit
        Book book = new Book(longTitle, "978-0-452-28423-4");
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with title too long should have validation errors");
    }
    
    @Test
    void testBookDescriptionTooLong() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        String longDescription = "A".repeat(2001); // Exceeds 2000 character limit
        book.setDescription(longDescription);
        
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertFalse(violations.isEmpty(), "Book with description too long should have validation errors");
    }
    
    @Test
    void testBookIsAvailable() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        book.setAvailableCopies(5);
        assertTrue(book.isAvailable(), "Book with available copies should be available");
        
        book.setAvailableCopies(0);
        assertFalse(book.isAvailable(), "Book with no available copies should not be available");
        
        book.setAvailableCopies(null);
        assertFalse(book.isAvailable(), "Book with null available copies should not be available");
    }
    
    @Test
    void testBookAuthorRelationship() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        Author author = new Author("John", "Doe");
        
        book.addAuthor(author);
        
        assertTrue(book.getAuthors().contains(author), "Book should contain the added author");
        assertTrue(author.getBooks().contains(book), "Author should contain the book");
        
        book.removeAuthor(author);
        
        assertFalse(book.getAuthors().contains(author), "Book should not contain the removed author");
        assertFalse(author.getBooks().contains(book), "Author should not contain the book");
    }
    
    @Test
    void testBookEqualsAndHashCode() {
        Book book1 = new Book("Test Title", "978-0-452-28423-4");
        Book book2 = new Book("Different Title", "978-0-452-28423-4");
        Book book3 = new Book("Test Title", "978-0-452-28424-1");
        
        assertEquals(book1, book2, "Books with same ISBN should be equal");
        assertNotEquals(book1, book3, "Books with different ISBN should not be equal");
        
        assertEquals(book1.hashCode(), book2.hashCode(), "Books with same ISBN should have same hash code");
    }
    
    @Test
    void testBookToString() {
        Book book = new Book("Test Title", "978-0-452-28423-4");
        book.setId(1L);
        book.setAvailableCopies(5);
        book.setTotalCopies(10);
        
        String toString = book.toString();
        assertTrue(toString.contains("Test Title"), "toString should contain title");
        assertTrue(toString.contains("978-0-452-28423-4"), "toString should contain ISBN");
        assertTrue(toString.contains("5"), "toString should contain available copies");
        assertTrue(toString.contains("10"), "toString should contain total copies");
    }
}