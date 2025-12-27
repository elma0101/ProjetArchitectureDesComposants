package com.bookstore.recommendation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "book_analytics")
public class BookAnalytics {
    
    @Id
    private String id;
    
    private Long bookId;
    
    @Builder.Default
    private int totalBorrows = 0;
    
    @Builder.Default
    private int activeBorrows = 0;
    
    @Builder.Default
    private double averageRating = 0.0;
    
    @Builder.Default
    private int totalRatings = 0;
    
    @Builder.Default
    private double popularityScore = 0.0;
    
    private LocalDateTime lastBorrowed;
    
    private LocalDateTime lastUpdated;
}
