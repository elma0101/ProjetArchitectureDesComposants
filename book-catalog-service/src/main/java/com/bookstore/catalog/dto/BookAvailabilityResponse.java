package com.bookstore.catalog.dto;

/**
 * DTO for book availability check response
 */
public class BookAvailabilityResponse {

    private Long bookId;
    private String title;
    private String isbn;
    private boolean available;
    private Integer availableCopies;
    private Integer totalCopies;

    // Constructors
    public BookAvailabilityResponse() {}

    public BookAvailabilityResponse(Long bookId, String title, String isbn, boolean available, 
                                   Integer availableCopies, Integer totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.available = available;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
    }

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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
}
