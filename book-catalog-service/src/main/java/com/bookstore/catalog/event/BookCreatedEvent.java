package com.bookstore.catalog.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Event published when a book is created
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookCreatedEvent extends BookEvent {
    private String title;
    private String description;
    private Integer publicationYear;
    private String genre;
    private Integer totalCopies;
    private Integer availableCopies;
    private Set<Long> authorIds;
    
    public BookCreatedEvent(Long bookId, String isbn, String title, String description,
                           Integer publicationYear, String genre, Integer totalCopies,
                           Integer availableCopies, Set<Long> authorIds, String correlationId) {
        super("BOOK_CREATED", bookId, isbn, correlationId);
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.authorIds = authorIds;
    }
}
