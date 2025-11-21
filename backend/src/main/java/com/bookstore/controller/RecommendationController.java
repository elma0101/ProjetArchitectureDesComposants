package com.bookstore.controller;

import com.bookstore.entity.Recommendation;
import com.bookstore.entity.RecommendationType;
import com.bookstore.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Book recommendation management")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get recommendations for a user", 
               description = "Generate and return personalized book recommendations for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<Recommendation>> getRecommendationsForUser(
            @Parameter(description = "User ID to get recommendations for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @PostMapping("/{userId}/generate")
    @Operation(summary = "Generate and save recommendations for a user", 
               description = "Generate personalized recommendations and save them to the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations generated and saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "500", description = "Error generating recommendations")
    })
    public ResponseEntity<List<Recommendation>> generateAndSaveRecommendations(
            @Parameter(description = "User ID to generate recommendations for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Maximum number of recommendations to generate")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId, limit);
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        return ResponseEntity.ok(savedRecommendations);
    }
    
    @GetMapping("/{userId}/saved")
    @Operation(summary = "Get saved recommendations for a user", 
               description = "Retrieve previously saved recommendations for a user from the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Saved recommendations retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    public ResponseEntity<Page<Recommendation>> getSavedRecommendationsForUser(
            @Parameter(description = "User ID to get saved recommendations for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Recommendation> recommendations = recommendationService.getRecommendationsForUser(userId, pageable);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular book recommendations", 
               description = "Get recommendations for popular books based on borrowing statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular recommendations retrieved successfully")
    })
    public ResponseEntity<List<Recommendation>> getPopularRecommendations(
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generatePopularRecommendations(limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/trending")
    @Operation(summary = "Get trending book recommendations", 
               description = "Get recommendations for trending books based on recent borrowing activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trending recommendations retrieved successfully")
    })
    public ResponseEntity<List<Recommendation>> getTrendingRecommendations(
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generateTrendingRecommendations(limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/collaborative/{userId}")
    @Operation(summary = "Get collaborative filtering recommendations", 
               description = "Get recommendations based on collaborative filtering algorithm")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Collaborative recommendations retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    public ResponseEntity<List<Recommendation>> getCollaborativeRecommendations(
            @Parameter(description = "User ID for collaborative filtering", required = true)
            @PathVariable String userId,
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generateCollaborativeFilteringRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/content-based/{userId}")
    @Operation(summary = "Get content-based recommendations", 
               description = "Get recommendations based on content-based filtering using genres and authors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Content-based recommendations retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    public ResponseEntity<List<Recommendation>> getContentBasedRecommendations(
            @Parameter(description = "User ID for content-based filtering", required = true)
            @PathVariable String userId,
            @Parameter(description = "Maximum number of recommendations to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        List<Recommendation> recommendations = recommendationService.generateContentBasedRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get recommendations by type", 
               description = "Get saved recommendations filtered by recommendation type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations by type retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid recommendation type")
    })
    public ResponseEntity<Page<Recommendation>> getRecommendationsByType(
            @Parameter(description = "Recommendation type", required = true)
            @PathVariable RecommendationType type,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Recommendation> recommendations = recommendationService.getRecommendationsByType(type, pageable);
        return ResponseEntity.ok(recommendations);
    }
    
    @DeleteMapping("/{userId}/cleanup")
    @Operation(summary = "Clean up old recommendations", 
               description = "Remove old recommendations for a user to keep the database clean")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Old recommendations cleaned up successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or days parameter")
    })
    public ResponseEntity<Void> cleanupOldRecommendations(
            @Parameter(description = "User ID to clean up recommendations for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Number of days to keep recommendations")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int daysToKeep) {
        
        recommendationService.cleanupOldRecommendations(userId, daysToKeep);
        return ResponseEntity.ok().build();
    }
}