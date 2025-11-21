package com.bookstore.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_book_isbn", columnList = "isbn"),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_genre", columnList = "genre"),
    @Index(name = "idx_book_publication_year", columnList = "publication_year"),
    @Index(name = "idx_book_available_copies", columnList = "available_copies"),
    @Index(name = "idx_book_created_at", columnList = "created_at")
})
@com.bookstore.validation.ValidCopyCount(groups = {com.bookstore.validation.ValidationGroups.Create.class, com.bookstore.validation.ValidationGroups.Update.class})
@Schema(description = "Book entity representing a book in the library system")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the book", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the book", example = "Clean Code: A Handbook of Agile Software Craftsmanship", required = true)
    private String title;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "ISBN is required", groups = {com.bookstore.validation.ValidationGroups.Create.class})
    @com.bookstore.validation.ISBN(groups = {com.bookstore.validation.ValidationGroups.Create.class, com.bookstore.validation.ValidationGroups.Update.class})
    @Schema(description = "International Standard Book Number", example = "978-0132350884", required = true)
    private String isbn;
    
    @Column(length = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the book", example = "A comprehensive guide to writing clean, maintainable code with practical examples and best practices.")
    private String description;
    
    @Column(name = "publication_year")
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year must be before 2100")
    @Schema(description = "Year the book was published", example = "2008", minimum = "1000", maximum = "2100")
    private Integer publicationYear;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Schema(description = "Genre or category of the book", example = "Programming")
    private String genre;
    
    @Column(name = "available_copies")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Schema(description = "Number of copies currently available for borrowing", example = "3", minimum = "0")
    private Integer availableCopies = 0;
    
    @Column(name = "total_copies")
    @Min(value = 0, message = "Total copies cannot be negative")
    @Schema(description = "Total number of copies owned by the library", example = "5", minimum = "0")
    private Integer totalCopies = 0;
    
    @Column(name = "image_url")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Schema(description = "URL or path to book cover image", example = "https://example.com/book-cover.jpg")
    private String imageUrl;
    
    @ManyToMany(mappedBy = "books", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Set<Author> authors = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Loan> loans = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Book() {}
    
    // Constructor with required fields
    public Book(String title, String isbn) {
        this.title = title;
        this.isbn = isbn;
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Set<Author> getAuthors() {
        return authors;
    }
    
    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }
    
    public Set<Loan> getLoans() {
        return loans;
    }
    
    public void setLoans(Set<Loan> loans) {
        this.loans = loans;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Helper methods
    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }
    
    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }
    
    public boolean isAvailable() {
        return availableCopies != null && availableCopies > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return isbn != null && isbn.equals(book.isbn);
    }
    
    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", availableCopies=" + availableCopies +
                ", totalCopies=" + totalCopies +
                '}';
    }
}