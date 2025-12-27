package com.bookstore.recommendation.controller;

import com.bookstore.recommendation.dto.RecommendationResponse;
import com.bookstore.recommendation.entity.RecommendationType;
import com.bookstore.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsForUser(
            @PathVariable Long userId) {
        log.info("GET /api/recommendations/user/{}", userId);
        
        List<RecommendationResponse> recommendations = 
            recommendationService.getRecommendationsForUser(userId);
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByType(
            @PathVariable Long userId,
            @PathVariable RecommendationType type) {
        log.info("GET /api/recommendations/user/{}/type/{}", userId, type);
        
        List<RecommendationResponse> recommendations = 
            recommendationService.getRecommendationsByType(userId, type);
        
        return ResponseEntity.ok(recommendations);
    }
    
    @PostMapping("/user/{userId}/refresh")
    public ResponseEntity<Void> refreshRecommendations(@PathVariable Long userId) {
        log.info("POST /api/recommendations/user/{}/refresh", userId);
        
        recommendationService.refreshRecommendations(userId);
        
        return ResponseEntity.ok().build();
    }
}
