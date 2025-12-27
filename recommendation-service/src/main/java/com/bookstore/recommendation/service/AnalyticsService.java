package com.bookstore.recommendation.service;

import com.bookstore.recommendation.dto.BookAnalyticsResponse;
import com.bookstore.recommendation.entity.BookAnalytics;
import com.bookstore.recommendation.repository.BookAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final BookAnalyticsRepository bookAnalyticsRepository;
    
    @Transactional(readOnly = true)
    public List<BookAnalyticsResponse> getPopularBooks(int limit) {
        log.info("Fetching top {} popular books", limit);
        
        List<BookAnalytics> analytics = bookAnalyticsRepository
            .findAllByOrderByPopularityScoreDesc(PageRequest.of(0, limit));
        
        return analytics.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BookAnalyticsResponse> getMostBorrowedBooks(int limit) {
        log.info("Fetching top {} most borrowed books", limit);
        
        List<BookAnalytics> analytics = bookAnalyticsRepository
            .findAllByOrderByTotalBorrowsDesc(PageRequest.of(0, limit));
        
        return analytics.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BookAnalyticsResponse> getTopRatedBooks(int limit) {
        log.info("Fetching top {} rated books", limit);
        
        List<BookAnalytics> analytics = bookAnalyticsRepository
            .findAllByOrderByAverageRatingDesc(PageRequest.of(0, limit));
        
        return analytics.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BookAnalyticsResponse getBookAnalytics(Long bookId) {
        log.info("Fetching analytics for book: {}", bookId);
        
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
            .orElseGet(() -> createDefaultAnalytics(bookId));
        
        return mapToResponse(analytics);
    }
    
    @Transactional
    public void recordBorrow(Long bookId) {
        log.info("Recording borrow for book: {}", bookId);
        
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
            .orElseGet(() -> createDefaultAnalytics(bookId));
        
        analytics.setTotalBorrows(analytics.getTotalBorrows() + 1);
        analytics.setActiveBorrows(analytics.getActiveBorrows() + 1);
        analytics.setLastBorrowed(LocalDateTime.now());
        analytics.setLastUpdated(LocalDateTime.now());
        
        // Update popularity score (simple algorithm: total borrows * 0.7 + average rating * 0.3)
        analytics.setPopularityScore(
            analytics.getTotalBorrows() * 0.7 + analytics.getAverageRating() * 0.3
        );
        
        bookAnalyticsRepository.save(analytics);
    }
    
    @Transactional
    public void recordReturn(Long bookId) {
        log.info("Recording return for book: {}", bookId);
        
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
            .orElseGet(() -> createDefaultAnalytics(bookId));
        
        analytics.setActiveBorrows(Math.max(0, analytics.getActiveBorrows() - 1));
        analytics.setLastUpdated(LocalDateTime.now());
        
        bookAnalyticsRepository.save(analytics);
    }
    
    @Transactional
    public void recordRating(Long bookId, int rating) {
        log.info("Recording rating {} for book: {}", rating, bookId);
        
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
            .orElseGet(() -> createDefaultAnalytics(bookId));
        
        // Calculate new average rating
        double currentTotal = analytics.getAverageRating() * analytics.getTotalRatings();
        int newTotalRatings = analytics.getTotalRatings() + 1;
        double newAverage = (currentTotal + rating) / newTotalRatings;
        
        analytics.setAverageRating(newAverage);
        analytics.setTotalRatings(newTotalRatings);
        analytics.setLastUpdated(LocalDateTime.now());
        
        // Update popularity score
        analytics.setPopularityScore(
            analytics.getTotalBorrows() * 0.7 + analytics.getAverageRating() * 0.3
        );
        
        bookAnalyticsRepository.save(analytics);
    }
    
    private BookAnalytics createDefaultAnalytics(Long bookId) {
        return BookAnalytics.builder()
            .bookId(bookId)
            .totalBorrows(0)
            .activeBorrows(0)
            .averageRating(0.0)
            .totalRatings(0)
            .popularityScore(0.0)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    private BookAnalyticsResponse mapToResponse(BookAnalytics analytics) {
        return BookAnalyticsResponse.builder()
            .id(analytics.getId())
            .bookId(analytics.getBookId())
            .totalBorrows(analytics.getTotalBorrows())
            .activeBorrows(analytics.getActiveBorrows())
            .averageRating(analytics.getAverageRating())
            .totalRatings(analytics.getTotalRatings())
            .popularityScore(analytics.getPopularityScore())
            .lastBorrowed(analytics.getLastBorrowed())
            .lastUpdated(analytics.getLastUpdated())
            .build();
    }
}
