package com.bookstore.recommendation.dto;

import com.bookstore.recommendation.entity.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private String id;
    private Long userId;
    private Long bookId;
    private Double score;
    private String reason;
    private RecommendationType type;
    private LocalDateTime createdAt;
    private BookInfo bookInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private Long id;
        private String title;
        private String isbn;
        private Integer publicationYear;
        private Integer availableCopies;
    }
}
