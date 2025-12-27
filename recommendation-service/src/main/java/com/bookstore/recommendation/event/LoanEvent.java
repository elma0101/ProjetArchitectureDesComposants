package com.bookstore.recommendation.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEvent {
    
    private Long loanId;
    private Long userId;
    private Long bookId;
    private String eventType; // BORROWED, RETURNED
    private LocalDateTime timestamp;
}
