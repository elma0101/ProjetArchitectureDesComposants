package com.bookstore.catalog.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when a book is deleted
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookDeletedEvent extends BookEvent {
    private String title;
    
    public BookDeletedEvent(Long bookId, String isbn, String title, String correlationId) {
        super("BOOK_DELETED", bookId, isbn, correlationId);
        this.title = title;
    }
}
