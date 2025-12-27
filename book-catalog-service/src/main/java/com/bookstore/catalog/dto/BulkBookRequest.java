package com.bookstore.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for bulk book operations
 */
public class BulkBookRequest {

    @NotEmpty(message = "Books list cannot be empty")
    @Valid
    private List<BookRequest> books;

    // Constructors
    public BulkBookRequest() {}

    public BulkBookRequest(List<BookRequest> books) {
        this.books = books;
    }

    // Getters and Setters
    public List<BookRequest> getBooks() {
        return books;
    }

    public void setBooks(List<BookRequest> books) {
        this.books = books;
    }
}
