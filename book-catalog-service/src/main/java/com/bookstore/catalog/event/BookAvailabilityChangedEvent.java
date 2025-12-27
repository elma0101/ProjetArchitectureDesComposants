package com.bookstore.catalog.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when book availability changes
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookAvailabilityChangedEvent extends BookEvent {
    private String title;
    private Integer previousAvailableCopies;
    private Integer currentAvailableCopies;
    private Integer totalCopies;
    private String changeReason;
    
    public BookAvailabilityChangedEvent(Long bookId, String isbn, String title,
                                       Integer previousAvailableCopies, 
                                       Integer currentAvailableCopies,
                                       Integer totalCopies,
                                       String changeReason,
                                       String correlationId) {
        super("BOOK_AVAILABILITY_CHANGED", bookId, isbn, correlationId);
        this.title = title;
        this.previousAvailableCopies = previousAvailableCopies;
        this.currentAvailableCopies = currentAvailableCopies;
        this.totalCopies = totalCopies;
        this.changeReason = changeReason;
    }
}
