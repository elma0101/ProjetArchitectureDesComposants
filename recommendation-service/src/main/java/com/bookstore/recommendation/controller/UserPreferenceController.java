package com.bookstore.recommendation.controller;

import com.bookstore.recommendation.dto.FeedbackRequest;
import com.bookstore.recommendation.dto.UserPreferenceResponse;
import com.bookstore.recommendation.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {
    
    private final UserPreferenceService userPreferenceService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserPreferenceResponse> getUserPreference(@PathVariable Long userId) {
        log.info("GET /api/preferences/user/{}", userId);
        
        UserPreferenceResponse preference = userPreferenceService.getUserPreference(userId);
        
        return ResponseEntity.ok(preference);
    }
    
    @PostMapping("/feedback")
    public ResponseEntity<UserPreferenceResponse> recordFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        log.info("POST /api/preferences/feedback");
        
        UserPreferenceResponse preference = userPreferenceService.recordFeedback(request);
        
        return ResponseEntity.ok(preference);
    }
    
    @PostMapping("/user/{userId}/favorite-author/{authorId}")
    public ResponseEntity<UserPreferenceResponse> addFavoriteAuthor(
            @PathVariable Long userId,
            @PathVariable Long authorId) {
        log.info("POST /api/preferences/user/{}/favorite-author/{}", userId, authorId);
        
        UserPreferenceResponse preference = 
            userPreferenceService.addFavoriteAuthor(userId, authorId);
        
        return ResponseEntity.ok(preference);
    }
    
    @PostMapping("/user/{userId}/favorite-category")
    public ResponseEntity<UserPreferenceResponse> addFavoriteCategory(
            @PathVariable Long userId,
            @RequestParam String category) {
        log.info("POST /api/preferences/user/{}/favorite-category?category={}", userId, category);
        
        UserPreferenceResponse preference = 
            userPreferenceService.addFavoriteCategory(userId, category);
        
        return ResponseEntity.ok(preference);
    }
}
