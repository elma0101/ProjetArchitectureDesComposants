package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Book availability status and notifications")
public class BookAvailabilityStatus {
    
    @Schema(description = "Book ID", example = "1")
    private Long bookId;
    
    @Schema(description = "Book title", example = "Clean Code")
    private String title;
    
    @Schema(description = "Number of available copies", example = "3")
    private Integer availableCopies;
    
    @Schema(description = "Total number of copies", example = "5")
    private Integer totalCopies;
    
    @Schema(description = "Whether the book is available for borrowing")
    private Boolean isAvailable;
    
    @Schema(description = "Availability percentage", example = "60.0")
    private Double availabilityPercentage;
    
    @Schema(description = "List of notifications about availability")
    private List<String> notifications;
    
    @Schema(description = "When this status was last updated")
    private LocalDateTime lastUpdated;
    
    // Constructors
    public BookAvailabilityStatus() {}
    
    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public Double getAvailabilityPercentage() {
        return availabilityPercentage;
    }
    
    public void setAvailabilityPercentage(Double availabilityPercentage) {
        this.availabilityPercentage = availabilityPercentage;
    }
    
    public List<String> getNotifications() {
        return notifications;
    }
    
    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}