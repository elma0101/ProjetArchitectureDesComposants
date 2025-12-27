package com.bookstore.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Event representing a loan-related event
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEvent {

    private Long loanId;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String userEmail;
    private String userName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String eventType; // CREATED, RETURNED, OVERDUE, DUE_SOON
}
