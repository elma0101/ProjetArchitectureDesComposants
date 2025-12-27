package com.bookstore.loanmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event received when book availability changes in the catalog service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAvailabilityChangedEvent {
    private String eventId;
    private String correlationId;
    private LocalDateTime timestamp;
    private String eventType;
    private Long bookId;
    private String isbn;
    private String title;
    private Integer previousAvailableCopies;
    private Integer currentAvailableCopies;
    private Integer totalCopies;
    private String changeReason;
}
