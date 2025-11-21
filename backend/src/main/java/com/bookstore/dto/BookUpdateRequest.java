package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Request for updating an existing book")
public class BookUpdateRequest {
    
    @NotNull(message = "Book ID is required")
    @Schema(description = "ID of the book to update", example = "1", required = true)
    private Long id;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the book", example = "Clean Code")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the book")
    private String description;
    
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year must be before 2100")
    @Schema(description = "Year the book was published", example = "2008")
    private Integer publicationYear;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Schema(description = "Genre or category of the book", example = "Programming")
    private String genre;
    
    @Min(value = 0, message = "Available copies cannot be negative")
    @Schema(description = "Number of copies available for borrowing", example = "3")
    private Integer availableCopies;
    
    @Min(value = 0, message = "Total copies cannot be negative")
    @Schema(description = "Total number of copies", example = "5")
    private Integer totalCopies;
    
    @Schema(description = "List of author IDs associated with this book")
    private List<Long> authorIds;
    
    @Schema(description = "URL or path to book cover image")
    private String imageUrl;
    
    // Constructors
    public BookUpdateRequest() {}
    
    public BookUpdateRequest(Long id) {
        this.id = id;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public Integer getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public Integer getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public List<Long> getAuthorIds() {
        return authorIds;
    }
    
    public void setAuthorIds(List<Long> authorIds) {
        this.authorIds = authorIds;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}