package com.bookstore.catalog.validation;

import com.bookstore.catalog.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CopyCountValidator
 */
class CopyCountValidatorTest {

    private CopyCountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CopyCountValidator();
    }

    @Test
    void testValid_AvailableLessThanTotal() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        assertTrue(validator.isValid(book, null));
    }

    @Test
    void testValid_AvailableEqualsTotal() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        assertTrue(validator.isValid(book, null));
    }

    @Test
    void testInvalid_AvailableGreaterThanTotal() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(5);
        book.setAvailableCopies(10);
        assertFalse(validator.isValid(book, null));
    }

    @Test
    void testValid_NullBook() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void testValid_NullAvailableCopies() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(5);
        book.setAvailableCopies(null);
        assertTrue(validator.isValid(book, null));
    }

    @Test
    void testValid_NullTotalCopies() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(null);
        book.setAvailableCopies(3);
        assertTrue(validator.isValid(book, null));
    }

    @Test
    void testValid_BothNull() {
        Book book = new Book("Test Book", "9780132350884");
        book.setTotalCopies(null);
        book.setAvailableCopies(null);
        assertTrue(validator.isValid(book, null));
    }
}
