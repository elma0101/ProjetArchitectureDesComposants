package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request for bulk book operations")
public class BulkBookRequest {
    
    @NotEmpty(message = "Books list cannot be empty")
    @Size(max = 100, message = "Cannot process more than 100 books at once")
    @Valid
    @Schema(description = "List of books to process", required = true)
    private List<BookCreateRequest> books;
    
    public BulkBookRequest() {}
    
    public BulkBookRequest(List<BookCreateRequest> books) {
        this.books = books;
    }
    
    public List<BookCreateRequest> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookCreateRequest> books) {
        this.books = books;
    }
}