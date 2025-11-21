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

class AuthorTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidAuthor() {
        Author author = new Author("John", "Doe");
        author.setBiography("A test author biography");
        author.setBirthDate(LocalDate.of(1980, 1, 1));
        author.setNationality("American");
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertTrue(violations.isEmpty(), "Valid author should have no validation errors");
    }
    
    @Test
    void testAuthorWithBlankFirstName() {
        Author author = new Author("", "Doe");
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with blank first name should have validation errors");
        
        boolean firstNameViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        assertTrue(firstNameViolationFound, "Should have first name validation error");
    }
    
    @Test
    void testAuthorWithBlankLastName() {
        Author author = new Author("John", "");
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with blank last name should have validation errors");
        
        boolean lastNameViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
        assertTrue(lastNameViolationFound, "Should have last name validation error");
    }
    
    @Test
    void testAuthorWithNullNames() {
        Author author = new Author();
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with null names should have validation errors");
        
        long nameViolations = violations.stream()
            .filter(v -> v.getPropertyPath().toString().equals("firstName") || 
                        v.getPropertyPath().toString().equals("lastName"))
            .count();
        assertEquals(2, nameViolations, "Should have both first name and last name validation errors");
    }
    
    @Test
    void testAuthorWithFutureBirthDate() {
        Author author = new Author("John", "Doe");
        author.setBirthDate(LocalDate.now().plusDays(1)); // Future date
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with future birth date should have validation errors");
        
        boolean birthDateViolationFound = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("birthDate"));
        assertTrue(birthDateViolationFound, "Should have birth date validation error");
    }
    
    @Test
    void testAuthorWithTooLongFirstName() {
        String longFirstName = "A".repeat(101); // Exceeds 100 character limit
        Author author = new Author(longFirstName, "Doe");
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with first name too long should have validation errors");
    }
    
    @Test
    void testAuthorWithTooLongLastName() {
        String longLastName = "A".repeat(101); // Exceeds 100 character limit
        Author author = new Author("John", longLastName);
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with last name too long should have validation errors");
    }
    
    @Test
    void testAuthorWithTooLongBiography() {
        Author author = new Author("John", "Doe");
        String longBiography = "A".repeat(1001); // Exceeds 1000 character limit
        author.setBiography(longBiography);
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with biography too long should have validation errors");
    }
    
    @Test
    void testAuthorWithTooLongNationality() {
        Author author = new Author("John", "Doe");
        String longNationality = "A".repeat(101); // Exceeds 100 character limit
        author.setNationality(longNationality);
        
        Set<ConstraintViolation<Author>> violations = validator.validate(author);
        assertFalse(violations.isEmpty(), "Author with nationality too long should have validation errors");
    }
    
    @Test
    void testAuthorBookRelationship() {
        Author author = new Author("John", "Doe");
        Book book = new Book("Test Title", "978-0-452-28423-4");
        
        author.addBook(book);
        
        assertTrue(author.getBooks().contains(book), "Author should contain the added book");
        assertTrue(book.getAuthors().contains(author), "Book should contain the author");
        
        author.removeBook(book);
        
        assertFalse(author.getBooks().contains(book), "Author should not contain the removed book");
        assertFalse(book.getAuthors().contains(author), "Book should not contain the author");
    }
    
    @Test
    void testAuthorGetFullName() {
        Author author = new Author("John", "Doe");
        assertEquals("John Doe", author.getFullName(), "Full name should be first name + space + last name");
        
        author.setFirstName("Jane");
        author.setLastName("Smith");
        assertEquals("Jane Smith", author.getFullName(), "Full name should update when names change");
    }
    
    @Test
    void testAuthorEqualsAndHashCode() {
        Author author1 = new Author("John", "Doe");
        author1.setId(1L);
        
        Author author2 = new Author("Jane", "Smith");
        author2.setId(1L);
        
        Author author3 = new Author("John", "Doe");
        author3.setId(2L);
        
        assertEquals(author1, author2, "Authors with same ID should be equal");
        assertNotEquals(author1, author3, "Authors with different ID should not be equal");
        
        assertEquals(author1.hashCode(), author2.hashCode(), "Authors with same ID should have same hash code");
    }
    
    @Test
    void testAuthorToString() {
        Author author = new Author("John", "Doe");
        author.setId(1L);
        author.setNationality("American");
        
        String toString = author.toString();
        assertTrue(toString.contains("John"), "toString should contain first name");
        assertTrue(toString.contains("Doe"), "toString should contain last name");
        assertTrue(toString.contains("American"), "toString should contain nationality");
    }
}