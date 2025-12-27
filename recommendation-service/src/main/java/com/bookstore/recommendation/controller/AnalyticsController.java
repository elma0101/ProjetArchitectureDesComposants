package com.bookstore.recommendation.controller;

import com.bookstore.recommendation.dto.BookAnalyticsResponse;
import com.bookstore.recommendation.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/popular-books")
    public ResponseEntity<List<BookAnalyticsResponse>> getPopularBooks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/popular-books?limit={}", limit);
        
        List<BookAnalyticsResponse> analytics = analyticsService.getPopularBooks(limit);
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/most-borrowed")
    public ResponseEntity<List<BookAnalyticsResponse>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/most-borrowed?limit={}", limit);
        
        List<BookAnalyticsResponse> analytics = analyticsService.getMostBorrowedBooks(limit);
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<BookAnalyticsResponse>> getTopRatedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/top-rated?limit={}", limit);
        
        List<BookAnalyticsResponse> analytics = analyticsService.getTopRatedBooks(limit);
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/book/{bookId}")
    public ResponseEntity<BookAnalyticsResponse> getBookAnalytics(@PathVariable Long bookId) {
        log.info("GET /api/analytics/book/{}", bookId);
        
        BookAnalyticsResponse analytics = analyticsService.getBookAnalytics(bookId);
        
        return ResponseEntity.ok(analytics);
    }
}
