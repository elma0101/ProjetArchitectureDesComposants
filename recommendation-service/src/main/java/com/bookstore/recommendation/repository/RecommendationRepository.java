package com.bookstore.recommendation.repository;

import com.bookstore.recommendation.entity.Recommendation;
import com.bookstore.recommendation.entity.RecommendationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    
    List<Recommendation> findByUserIdAndActiveTrue(Long userId);
    
    List<Recommendation> findByUserIdAndTypeAndActiveTrue(Long userId, RecommendationType type);
    
    List<Recommendation> findByUserIdAndActiveTrueOrderByScoreDesc(Long userId);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
