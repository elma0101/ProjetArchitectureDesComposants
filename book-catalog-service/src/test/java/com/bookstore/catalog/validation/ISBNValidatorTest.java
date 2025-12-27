package com.bookstore.catalog.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ISBNValidator
 */
class ISBNValidatorTest {

    private ISBNValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ISBNValidator();
    }

    @Test
    void testValidISBN10() {
        // Valid ISBN-10 examples with correct check digits
        assertTrue(validator.isValid("0306406152", null));
        assertTrue(validator.isValid("043942089X", null));
    }

    @Test
    void testValidISBN10WithHyphens() {
        assertTrue(validator.isValid("0-306-40615-2", null));
        assertTrue(validator.isValid("0-439-42089-X", null));
    }

    @Test
    void testValidISBN13() {
        assertTrue(validator.isValid("9780132350884", null));
        assertTrue(validator.isValid("9780451524935", null));
    }

    @Test
    void testValidISBN13WithHyphens() {
        assertTrue(validator.isValid("978-0-13-235088-4", null));
        assertTrue(validator.isValid("978-0-451-52493-5", null));
    }

    @Test
    void testInvalidISBN10_WrongCheckDigit() {
        assertFalse(validator.isValid("0306406153", null));
    }

    @Test
    void testInvalidISBN13_WrongCheckDigit() {
        assertFalse(validator.isValid("9780132350885", null));
    }

    @Test
    void testInvalidISBN_WrongLength() {
        assertFalse(validator.isValid("123456789", null));
        assertFalse(validator.isValid("12345678901234", null));
    }

    @Test
    void testInvalidISBN_NullValue() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void testInvalidISBN_EmptyString() {
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid("   ", null));
    }

    @Test
    void testInvalidISBN_InvalidCharacters() {
        assertFalse(validator.isValid("012345678A", null));
        assertFalse(validator.isValid("978012345678A", null));
    }
}
