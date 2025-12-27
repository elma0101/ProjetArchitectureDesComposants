package com.bookstore.recommendation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_preferences")
public class UserPreference {
    
    @Id
    private String id;
    
    private Long userId;
    
    @Builder.Default
    private List<String> favoriteCategories = new ArrayList<>();
    
    @Builder.Default
    private List<Long> favoriteAuthors = new ArrayList<>();
    
    @Builder.Default
    private List<Long> borrowedBooks = new ArrayList<>();
    
    @Builder.Default
    private Map<Long, Integer> bookRatings = new HashMap<>();
    
    private LocalDateTime lastUpdated;
    
    @Builder.Default
    private int totalBorrows = 0;
}
