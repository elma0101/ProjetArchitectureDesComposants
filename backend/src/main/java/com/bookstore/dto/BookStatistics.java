package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Book statistics and reporting data")
public class BookStatistics {
    
    @Schema(description = "Total number of books in the system", example = "150")
    private Long totalBooks;
    
    @Schema(description = "Total number of copies across all books", example = "500")
    private Integer totalCopies;
    
    @Schema(description = "Total number of available copies", example = "320")
    private Integer availableCopies;
    
    @Schema(description = "Number of books that are out of stock", example = "5")
    private Integer outOfStockBooks;
    
    @Schema(description = "Distribution of books by genre")
    private Map<String, Long> genreDistribution;
    
    @Schema(description = "Distribution of books by publication year")
    private Map<Integer, Long> publicationYearDistribution;
    
    @Schema(description = "When these statistics were generated")
    private LocalDateTime generatedAt;
    
    // Constructors
    public BookStatistics() {}
    
    // Getters and Setters
    public Long getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(Long totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public Integer getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public Integer getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public Integer getOutOfStockBooks() {
        return outOfStockBooks;
    }
    
    public void setOutOfStockBooks(Integer outOfStockBooks) {
        this.outOfStockBooks = outOfStockBooks;
    }
    
    public Map<String, Long> getGenreDistribution() {
        return genreDistribution;
    }
    
    public void setGenreDistribution(Map<String, Long> genreDistribution) {
        this.genreDistribution = genreDistribution;
    }
    
    public Map<Integer, Long> getPublicationYearDistribution() {
        return publicationYearDistribution;
    }
    
    public void setPublicationYearDistribution(Map<Integer, Long> publicationYearDistribution) {
        this.publicationYearDistribution = publicationYearDistribution;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    // Calculated properties
    @Schema(description = "Percentage of books that are available", example = "64.0")
    public Double getAvailabilityRate() {
        if (totalCopies == null || totalCopies == 0) {
            return 0.0;
        }
        return (double) availableCopies / totalCopies * 100;
    }
    
    @Schema(description = "Percentage of books that are out of stock", example = "3.3")
    public Double getOutOfStockRate() {
        if (totalBooks == null || totalBooks == 0) {
            return 0.0;
        }
        return (double) outOfStockBooks / totalBooks * 100;
    }
}