package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statistics about an author")
public class AuthorStatistics {
    
    @Schema(description = "Author ID", example = "1")
    private Long authorId;
    
    @Schema(description = "Author's full name", example = "Robert Martin")
    private String fullName;
    
    @Schema(description = "Total number of books written by the author", example = "5")
    private Long totalBooks;
    
    @Schema(description = "Total number of copies of all books by the author", example = "25")
    private Long totalCopies;
    
    @Schema(description = "Total number of available copies of all books by the author", example = "15")
    private Long availableCopies;
    
    @Schema(description = "Total number of times books by this author have been borrowed", example = "45")
    private Long totalLoans;
    
    @Schema(description = "Number of currently active loans for books by this author", example = "8")
    private Long activeLoans;
    
    @Schema(description = "Average rating of books by this author", example = "4.5")
    private Double averageRating;
    
    // Default constructor
    public AuthorStatistics() {}
    
    // Constructor
    public AuthorStatistics(Long authorId, String fullName, Long totalBooks) {
        this.authorId = authorId;
        this.fullName = fullName;
        this.totalBooks = totalBooks;
    }
    
    // Getters and Setters
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Long getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(Long totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public Long getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(Long totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public Long getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(Long availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public Long getTotalLoans() {
        return totalLoans;
    }
    
    public void setTotalLoans(Long totalLoans) {
        this.totalLoans = totalLoans;
    }
    
    public Long getActiveLoans() {
        return activeLoans;
    }
    
    public void setActiveLoans(Long activeLoans) {
        this.activeLoans = activeLoans;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    @Override
    public String toString() {
        return "AuthorStatistics{" +
                "authorId=" + authorId +
                ", fullName='" + fullName + '\'' +
                ", totalBooks=" + totalBooks +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                ", totalLoans=" + totalLoans +
                ", activeLoans=" + activeLoans +
                ", averageRating=" + averageRating +
                '}';
    }
}