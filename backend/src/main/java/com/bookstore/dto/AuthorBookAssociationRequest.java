package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Schema(description = "Request object for managing author-book associations")
public class AuthorBookAssociationRequest {
    
    @NotNull(message = "Book IDs are required")
    @Schema(description = "Set of book IDs to associate with the author", example = "[1, 2, 3]", required = true)
    private Set<Long> bookIds;
    
    // Default constructor
    public AuthorBookAssociationRequest() {}
    
    // Constructor
    public AuthorBookAssociationRequest(Set<Long> bookIds) {
        this.bookIds = bookIds;
    }
    
    // Getters and Setters
    public Set<Long> getBookIds() {
        return bookIds;
    }
    
    public void setBookIds(Set<Long> bookIds) {
        this.bookIds = bookIds;
    }
    
    @Override
    public String toString() {
        return "AuthorBookAssociationRequest{" +
                "bookIds=" + bookIds +
                '}';
    }
}