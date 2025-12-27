package com.bookstore.catalog.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Author entity
 */
class AuthorTest {

    private Author author;
    private Book book;

    @BeforeEach
    void setUp() {
        author = new Author("John", "Doe");
        author.setBiography("A test author");
        author.setNationality("American");
        author.setBirthDate(LocalDate.of(1970, 1, 1));
        
        book = new Book("Test Book", "9780132350884");
    }

    @Test
    void testAuthorCreation() {
        assertNotNull(author);
        assertEquals("John", author.getFirstName());
        assertEquals("Doe", author.getLastName());
    }

    @Test
    void testGetFullName() {
        assertEquals("John Doe", author.getFullName());
    }

    @Test
    void testAddBook() {
        author.addBook(book);
        assertTrue(author.getBooks().contains(book));
        assertTrue(book.getAuthors().contains(author));
    }

    @Test
    void testRemoveBook() {
        author.addBook(book);
        author.removeBook(book);
        assertFalse(author.getBooks().contains(book));
        assertFalse(book.getAuthors().contains(author));
    }

    @Test
    void testAddBook_NullBook() {
        author.addBook(null);
        assertEquals(0, author.getBooks().size());
    }

    @Test
    void testRemoveBook_NullBook() {
        author.addBook(book);
        author.removeBook(null);
        assertEquals(1, author.getBooks().size());
    }

    @Test
    void testEquals_SameId() {
        Author author1 = new Author("John", "Doe");
        author1.setId(1L);
        Author author2 = new Author("Jane", "Smith");
        author2.setId(1L);
        assertEquals(author1, author2);
    }

    @Test
    void testEquals_DifferentId() {
        Author author1 = new Author("John", "Doe");
        author1.setId(1L);
        Author author2 = new Author("John", "Doe");
        author2.setId(2L);
        assertNotEquals(author1, author2);
    }

    @Test
    void testEquals_NullId() {
        Author author1 = new Author("John", "Doe");
        Author author2 = new Author("John", "Doe");
        // When both IDs are null, they should not be equal (different instances)
        assertNotEquals(author1, author2);
    }
}
