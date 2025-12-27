package com.bookstore.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {
    
    private String id;
    private Long userId;
    private List<String> favoriteCategories;
    private List<Long> favoriteAuthors;
    private List<Long> borrowedBooks;
    private Map<Long, Integer> bookRatings;
    private LocalDateTime lastUpdated;
    private int totalBorrows;
}
