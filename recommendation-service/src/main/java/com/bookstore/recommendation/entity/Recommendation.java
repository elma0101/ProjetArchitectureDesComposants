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
@Document(collection = "recommendations")
public class Recommendation {
    
    @Id
    private String id;
    
    private Long userId;
    
    private Long bookId;
    
    private Double score;
    
    private String reason;
    
    private RecommendationType type;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private boolean active;
}
