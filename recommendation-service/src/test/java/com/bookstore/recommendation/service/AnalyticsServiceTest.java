package com.bookstore.recommendation.service;

import com.bookstore.recommendation.dto.BookAnalyticsResponse;
import com.bookstore.recommendation.entity.BookAnalytics;
import com.bookstore.recommendation.repository.BookAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    
    @Mock
    private BookAnalyticsRepository bookAnalyticsRepository;
    
    @InjectMocks
    private AnalyticsService analyticsService;
    
    private BookAnalytics bookAnalytics;
    
    @BeforeEach
    void setUp() {
        bookAnalytics = BookAnalytics.builder()
            .id("analytics1")
            .bookId(1L)
            .totalBorrows(100)
            .activeBorrows(5)
            .averageRating(4.5)
            .totalRatings(20)
            .popularityScore(85.0)
            .lastBorrowed(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    @Test
    void testGetPopularBooks() {
        // Given
        when(bookAnalyticsRepository.findAllByOrderByPopularityScoreDesc(any(PageRequest.class)))
            .thenReturn(List.of(bookAnalytics));
        
        // When
        List<BookAnalyticsResponse> result = analyticsService.getPopularBooks(10);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
        assertThat(result.get(0).getPopularityScore()).isEqualTo(85.0);
    }
    
    @Test
    void testGetMostBorrowedBooks() {
        // Given
        when(bookAnalyticsRepository.findAllByOrderByTotalBorrowsDesc(any(PageRequest.class)))
            .thenReturn(List.of(bookAnalytics));
        
        // When
        List<BookAnalyticsResponse> result = analyticsService.getMostBorrowedBooks(10);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalBorrows()).isEqualTo(100);
    }
    
    @Test
    void testRecordBorrow() {
        // Given
        when(bookAnalyticsRepository.findByBookId(1L))
            .thenReturn(Optional.of(bookAnalytics));
        when(bookAnalyticsRepository.save(any(BookAnalytics.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        analyticsService.recordBorrow(1L);
        
        // Then
        verify(bookAnalyticsRepository).save(any(BookAnalytics.class));
    }
    
    @Test
    void testRecordReturn() {
        // Given
        when(bookAnalyticsRepository.findByBookId(1L))
            .thenReturn(Optional.of(bookAnalytics));
        when(bookAnalyticsRepository.save(any(BookAnalytics.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        analyticsService.recordReturn(1L);
        
        // Then
        verify(bookAnalyticsRepository).save(any(BookAnalytics.class));
    }
    
    @Test
    void testRecordRating() {
        // Given
        when(bookAnalyticsRepository.findByBookId(1L))
            .thenReturn(Optional.of(bookAnalytics));
        when(bookAnalyticsRepository.save(any(BookAnalytics.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        analyticsService.recordRating(1L, 5);
        
        // Then
        verify(bookAnalyticsRepository).save(any(BookAnalytics.class));
    }
}
