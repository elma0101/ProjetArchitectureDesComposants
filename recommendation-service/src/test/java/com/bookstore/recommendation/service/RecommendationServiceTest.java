package com.bookstore.recommendation.service;

import com.bookstore.recommendation.client.BookCatalogClient;
import com.bookstore.recommendation.dto.BookResponse;
import com.bookstore.recommendation.dto.RecommendationResponse;
import com.bookstore.recommendation.entity.BookAnalytics;
import com.bookstore.recommendation.entity.Recommendation;
import com.bookstore.recommendation.entity.RecommendationType;
import com.bookstore.recommendation.entity.UserPreference;
import com.bookstore.recommendation.repository.BookAnalyticsRepository;
import com.bookstore.recommendation.repository.RecommendationRepository;
import com.bookstore.recommendation.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    
    @Mock
    private RecommendationRepository recommendationRepository;
    
    @Mock
    private UserPreferenceRepository userPreferenceRepository;
    
    @Mock
    private BookAnalyticsRepository bookAnalyticsRepository;
    
    @Mock
    private BookCatalogClient bookCatalogClient;
    
    @InjectMocks
    private RecommendationService recommendationService;
    
    private Long userId;
    private Recommendation recommendation;
    private BookAnalytics bookAnalytics;
    
    @BeforeEach
    void setUp() {
        userId = 1L;
        
        recommendation = Recommendation.builder()
            .id("rec1")
            .userId(userId)
            .bookId(1L)
            .score(0.9)
            .reason("Popular book")
            .type(RecommendationType.POPULAR)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .active(true)
            .build();
        
        bookAnalytics = BookAnalytics.builder()
            .id("analytics1")
            .bookId(1L)
            .totalBorrows(100)
            .popularityScore(85.0)
            .build();
    }
    
    @Test
    void testGetRecommendationsForUser_WithExistingRecommendations() {
        // Given
        List<Recommendation> existingRecs = List.of(recommendation);
        when(recommendationRepository.findByUserIdAndActiveTrueOrderByScoreDesc(userId))
            .thenReturn(existingRecs);
        when(bookCatalogClient.getBookById(1L))
            .thenReturn(createBookResponse(1L, "Test Book"));
        
        // When
        List<RecommendationResponse> result = recommendationService.getRecommendationsForUser(userId);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
        verify(recommendationRepository, never()).saveAll(any());
    }
    
    @Test
    void testGetRecommendationsForUser_GeneratesNewRecommendations() {
        // Given
        when(recommendationRepository.findByUserIdAndActiveTrueOrderByScoreDesc(userId))
            .thenReturn(new ArrayList<>());
        when(userPreferenceRepository.findByUserId(userId))
            .thenReturn(Optional.empty());
        when(bookAnalyticsRepository.findAllByOrderByPopularityScoreDesc(any(PageRequest.class)))
            .thenReturn(List.of(bookAnalytics));
        when(recommendationRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookCatalogClient.getBookById(1L))
            .thenReturn(createBookResponse(1L, "Test Book"));
        
        // When
        List<RecommendationResponse> result = recommendationService.getRecommendationsForUser(userId);
        
        // Then
        assertThat(result).isNotEmpty();
        verify(recommendationRepository).saveAll(any());
    }
    
    @Test
    void testGetRecommendationsByType_Popular() {
        // Given
        when(recommendationRepository.findByUserIdAndTypeAndActiveTrue(userId, RecommendationType.POPULAR))
            .thenReturn(new ArrayList<>());
        when(bookAnalyticsRepository.findAllByOrderByPopularityScoreDesc(any(PageRequest.class)))
            .thenReturn(List.of(bookAnalytics));
        when(recommendationRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookCatalogClient.getBookById(1L))
            .thenReturn(createBookResponse(1L, "Test Book"));
        
        // When
        List<RecommendationResponse> result = 
            recommendationService.getRecommendationsByType(userId, RecommendationType.POPULAR);
        
        // Then
        assertThat(result).isNotEmpty();
        verify(bookAnalyticsRepository).findAllByOrderByPopularityScoreDesc(any(PageRequest.class));
    }
    
    @Test
    void testRefreshRecommendations() {
        // Given
        List<Recommendation> existingRecs = List.of(recommendation);
        when(recommendationRepository.findByUserIdAndActiveTrue(userId))
            .thenReturn(existingRecs);
        when(userPreferenceRepository.findByUserId(userId))
            .thenReturn(Optional.empty());
        when(bookAnalyticsRepository.findAllByOrderByPopularityScoreDesc(any(PageRequest.class)))
            .thenReturn(List.of(bookAnalytics));
        when(recommendationRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        recommendationService.refreshRecommendations(userId);
        
        // Then
        verify(recommendationRepository, times(2)).saveAll(any());
    }
    
    private BookResponse createBookResponse(Long id, String title) {
        return BookResponse.builder()
            .id(id)
            .title(title)
            .isbn("1234567890")
            .publicationYear(2020)
            .totalCopies(10)
            .availableCopies(5)
            .build();
    }
}
