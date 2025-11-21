package com.bookstore.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ISBNValidatorTest {

    private ISBNValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ISBNValidator();
        validator.initialize(null);
    }

    @Test
    void shouldValidateValidISBN10() {
        // Valid ISBN-10 examples
        assertThat(validator.isValid("0123456789", null)).isTrue();
        assertThat(validator.isValid("0-123-45678-9", null)).isTrue();
        assertThat(validator.isValid("0 123 45678 9", null)).isTrue();
        assertThat(validator.isValid("043942089X", null)).isTrue();
    }

    @Test
    void shouldValidateValidISBN13() {
        // Valid ISBN-13 examples
        assertThat(validator.isValid("9780123456786", null)).isTrue();
        assertThat(validator.isValid("978-0-123-45678-6", null)).isTrue();
        assertThat(validator.isValid("978 0 123 45678 6", null)).isTrue();
    }

    @Test
    void shouldRejectInvalidISBN10() {
        // Invalid ISBN-10 examples
        assertThat(validator.isValid("0123456788", null)).isFalse(); // Wrong check digit
        assertThat(validator.isValid("012345678", null)).isFalse();  // Too short
        assertThat(validator.isValid("01234567890", null)).isFalse(); // Too long
        assertThat(validator.isValid("012345678A", null)).isFalse(); // Invalid character
    }

    @Test
    void shouldRejectInvalidISBN13() {
        // Invalid ISBN-13 examples
        assertThat(validator.isValid("9780123456785", null)).isFalse(); // Wrong check digit
        assertThat(validator.isValid("978012345678", null)).isFalse();  // Too short
        assertThat(validator.isValid("97801234567890", null)).isFalse(); // Too long
        assertThat(validator.isValid("978012345678A", null)).isFalse(); // Invalid character
    }

    @Test
    void shouldRejectNullAndEmptyValues() {
        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
    }

    @Test
    void shouldRejectInvalidLength() {
        assertThat(validator.isValid("123", null)).isFalse();
        assertThat(validator.isValid("12345678901234", null)).isFalse();
    }
}