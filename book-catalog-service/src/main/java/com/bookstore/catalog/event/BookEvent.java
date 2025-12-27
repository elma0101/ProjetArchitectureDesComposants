package com.bookstore.catalog.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base class for book-related events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BookEvent {
    private String eventId;
    private String correlationId;
    private LocalDateTime timestamp;
    private String eventType;
    private Long bookId;
    private String isbn;
    
    public BookEvent(String eventType, Long bookId, String isbn, String correlationId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.bookId = bookId;
        this.isbn = isbn;
    }
}
