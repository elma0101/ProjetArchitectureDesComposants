package com.bookstore.validation;

import com.bookstore.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopyCountValidatorTest {

    private CopyCountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CopyCountValidator();
        validator.initialize(null);
    }

    @Test
    void shouldValidateValidCopyCount() {
        Book book = new Book();
        book.setTotalCopies(10);
        book.setAvailableCopies(5);

        assertThat(validator.isValid(book, null)).isTrue();
    }

    @Test
    void shouldValidateEqualCopyCounts() {
        Book book = new Book();
        book.setTotalCopies(10);
        book.setAvailableCopies(10);

        assertThat(validator.isValid(book, null)).isTrue();
    }

    @Test
    void shouldValidateZeroCopies() {
        Book book = new Book();
        book.setTotalCopies(0);
        book.setAvailableCopies(0);

        assertThat(validator.isValid(book, null)).isTrue();
    }

    @Test
    void shouldRejectInvalidCopyCount() {
        Book book = new Book();
        book.setTotalCopies(5);
        book.setAvailableCopies(10);

        assertThat(validator.isValid(book, null)).isFalse();
    }

    @Test
    void shouldValidateNullCopies() {
        Book book1 = new Book();
        book1.setTotalCopies(null);
        book1.setAvailableCopies(5);

        Book book2 = new Book();
        book2.setTotalCopies(10);
        book2.setAvailableCopies(null);

        Book book3 = new Book();
        book3.setTotalCopies(null);
        book3.setAvailableCopies(null);

        assertThat(validator.isValid(book1, null)).isTrue();
        assertThat(validator.isValid(book2, null)).isTrue();
        assertThat(validator.isValid(book3, null)).isTrue();
    }

    @Test
    void shouldValidateNullBook() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}