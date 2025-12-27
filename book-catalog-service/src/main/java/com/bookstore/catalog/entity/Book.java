package com.bookstore.catalog.entity;

import com.bookstore.catalog.validation.ISBN;
import com.bookstore.catalog.validation.ValidCopyCount;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Book entity representing a book in the catalog service
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_book_isbn", columnList = "isbn"),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_genre", columnList = "genre"),
    @Index(name = "idx_book_publication_year", columnList = "publication_year"),
    @Index(name = "idx_book_available_copies", columnList = "available_copies"),
    @Index(name = "idx_book_created_at", columnList = "created_at")
})
@ValidCopyCount
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "ISBN is required")
    @ISBN
    private String isbn;

    @Column(length = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Column(name = "publication_year")
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year must be before 2100")
    private Integer publicationYear;

    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    @Column(name = "available_copies")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies = 0;

    @Column(name = "total_copies")
    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies = 0;

    @Column(name = "image_url")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonManagedReference
    private Set<Author> authors = new HashSet<>();

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business logic methods

    /**
     * Add an author to this book and maintain bidirectional relationship
     */
    public void addAuthor(Author author) {
        if (author != null) {
            authors.add(author);
            author.getBooks().add(this);
        }
    }

    /**
     * Remove an author from this book and maintain bidirectional relationship
     */
    public void removeAuthor(Author author) {
        if (author != null) {
            authors.remove(author);
            author.getBooks().remove(this);
        }
    }

    /**
     * Check if the book is available for borrowing
     */
    public boolean isAvailable() {
        return availableCopies != null && availableCopies > 0;
    }

    /**
     * Decrement available copies when a book is borrowed
     * @throws IllegalStateException if no copies are available
     */
    public void decrementAvailableCopies() {
        if (availableCopies == null || availableCopies <= 0) {
            throw new IllegalStateException("No available copies to decrement");
        }
        availableCopies--;
    }

    /**
     * Increment available copies when a book is returned
     * @throws IllegalStateException if available copies would exceed total copies
     */
    public void incrementAvailableCopies() {
        if (availableCopies == null) {
            availableCopies = 0;
        }
        if (totalCopies == null || availableCopies >= totalCopies) {
            throw new IllegalStateException("Available copies cannot exceed total copies");
        }
        availableCopies++;
    }

    /**
     * Validate that available copies does not exceed total copies
     * @return true if valid, false otherwise
     */
    public boolean validateCopyCount() {
        if (availableCopies == null || totalCopies == null) {
            return true;
        }
        return availableCopies <= totalCopies;
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
