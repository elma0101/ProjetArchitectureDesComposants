package com.bookstore.catalog.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Book entity
 */
class BookTest {

    private Book book;
    private Author author;

    @BeforeEach
    void setUp() {
        book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        
        author = new Author("John", "Doe");
    }

    @Test
    void testBookCreation() {
        assertNotNull(book);
        assertEquals("Test Book", book.getTitle());
        assertEquals("9780132350884", book.getIsbn());
    }

    @Test
    void testIsAvailable_WhenCopiesAvailable() {
        assertTrue(book.isAvailable());
    }

    @Test
    void testIsAvailable_WhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        assertFalse(book.isAvailable());
    }

    @Test
    void testDecrementAvailableCopies_Success() {
        int initialCopies = book.getAvailableCopies();
        book.decrementAvailableCopies();
        assertEquals(initialCopies - 1, book.getAvailableCopies());
    }

    @Test
    void testDecrementAvailableCopies_ThrowsException_WhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        assertThrows(IllegalStateException.class, () -> book.decrementAvailableCopies());
    }

    @Test
    void testIncrementAvailableCopies_Success() {
        int initialCopies = book.getAvailableCopies();
        book.incrementAvailableCopies();
        assertEquals(initialCopies + 1, book.getAvailableCopies());
    }

    @Test
    void testIncrementAvailableCopies_ThrowsException_WhenExceedsTotalCopies() {
        book.setAvailableCopies(5);
        assertThrows(IllegalStateException.class, () -> book.incrementAvailableCopies());
    }

    @Test
    void testAddAuthor() {
        book.addAuthor(author);
        assertTrue(book.getAuthors().contains(author));
        assertTrue(author.getBooks().contains(book));
    }

    @Test
    void testRemoveAuthor() {
        book.addAuthor(author);
        book.removeAuthor(author);
        assertFalse(book.getAuthors().contains(author));
        assertFalse(author.getBooks().contains(book));
    }

    @Test
    void testValidateCopyCount_Valid() {
        assertTrue(book.validateCopyCount());
    }

    @Test
    void testValidateCopyCount_Invalid() {
        book.setAvailableCopies(10);
        book.setTotalCopies(5);
        assertFalse(book.validateCopyCount());
    }

    @Test
    void testEquals_SameIsbn() {
        Book book1 = new Book("Book 1", "9780132350884");
        Book book2 = new Book("Book 2", "9780132350884");
        assertEquals(book1, book2);
    }

    @Test
    void testEquals_DifferentIsbn() {
        Book book1 = new Book("Book 1", "9780132350884");
        Book book2 = new Book("Book 2", "9780132350891");
        assertNotEquals(book1, book2);
    }

    @Test
    void testHashCode_SameIsbn() {
        Book book1 = new Book("Book 1", "9780132350884");
        Book book2 = new Book("Book 2", "9780132350884");
        assertEquals(book1.hashCode(), book2.hashCode());
    }
}
