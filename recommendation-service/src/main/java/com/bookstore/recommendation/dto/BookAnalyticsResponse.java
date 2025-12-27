package com.bookstore.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAnalyticsResponse {
    
    private String id;
    private Long bookId;
    private int totalBorrows;
    private int activeBorrows;
    private double averageRating;
    private int totalRatings;
    private double popularityScore;
    private LocalDateTime lastBorrowed;
    private LocalDateTime lastUpdated;
}
