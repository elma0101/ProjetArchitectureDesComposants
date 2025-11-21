package com.bookstore.dto;

import com.bookstore.validation.ISBN;
import com.bookstore.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Request for creating a new book")
public class BookCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the book", example = "Clean Code", required = true)
    private String title;
    
    @NotBlank(message = "ISBN is required")
    @ISBN(groups = {ValidationGroups.Create.class})
    @Schema(description = "International Standard Book Number", example = "978-0132350884", required = true)
    private String isbn;
    
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
    private Integer availableCopies = 0;
    
    @Min(value = 0, message = "Total copies cannot be negative")
    @Schema(description = "Total number of copies", example = "5")
    private Integer totalCopies = 0;
    
    @Schema(description = "List of author IDs associated with this book")
    private List<Long> authorIds;
    
    @Schema(description = "URL or path to book cover image")
    private String imageUrl;
    
    // Constructors
    public BookCreateRequest() {}
    
    public BookCreateRequest(String title, String isbn) {
        this.title = title;
        this.isbn = isbn;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
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