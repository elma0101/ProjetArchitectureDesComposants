package com.bookstore.catalog.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Event published when a book is updated
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookUpdatedEvent extends BookEvent {
    private String title;
    private String description;
    private Integer publicationYear;
    private String genre;
    private Integer totalCopies;
    private Integer availableCopies;
    private Set<Long> authorIds;
    private Integer previousAvailableCopies;
    
    public BookUpdatedEvent(Long bookId, String isbn, String title, String description,
                           Integer publicationYear, String genre, Integer totalCopies,
                           Integer availableCopies, Set<Long> authorIds, 
                           Integer previousAvailableCopies, String correlationId) {
        super("BOOK_UPDATED", bookId, isbn, correlationId);
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.authorIds = authorIds;
        this.previousAvailableCopies = previousAvailableCopies;
    }
}
