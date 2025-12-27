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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final BookAnalyticsRepository bookAnalyticsRepository;
    private final BookCatalogClient bookCatalogClient;
    
    private static final int MAX_RECOMMENDATIONS = 10;
    private static final int RECOMMENDATION_EXPIRY_DAYS = 7;
    
    @Transactional
    public List<RecommendationResponse> getRecommendationsForUser(Long userId) {
        log.info("Fetching recommendations for user: {}", userId);
        
        // Get existing active recommendations
        List<Recommendation> existingRecommendations = 
            recommendationRepository.findByUserIdAndActiveTrueOrderByScoreDesc(userId);
        
        // If we have recent recommendations, return them
        if (!existingRecommendations.isEmpty()) {
            return mapToResponseList(existingRecommendations);
        }
        
        // Generate new recommendations
        List<Recommendation> newRecommendations = generateRecommendations(userId);
        
        // Save recommendations
        recommendationRepository.saveAll(newRecommendations);
        
        return mapToResponseList(newRecommendations);
    }
    
    @Transactional
    public List<RecommendationResponse> getRecommendationsByType(Long userId, RecommendationType type) {
        log.info("Fetching {} recommendations for user: {}", type, userId);
        
        List<Recommendation> recommendations = 
            recommendationRepository.findByUserIdAndTypeAndActiveTrue(userId, type);
        
        if (recommendations.isEmpty()) {
            recommendations = generateRecommendationsByType(userId, type);
            recommendationRepository.saveAll(recommendations);
        }
        
        return mapToResponseList(recommendations);
    }
    
    @Transactional
    public void refreshRecommendations(Long userId) {
        log.info("Refreshing recommendations for user: {}", userId);
        
        // Delete existing recommendations
        List<Recommendation> existing = recommendationRepository.findByUserIdAndActiveTrue(userId);
        existing.forEach(rec -> rec.setActive(false));
        recommendationRepository.saveAll(existing);
        
        // Generate new recommendations
        List<Recommendation> newRecommendations = generateRecommendations(userId);
        recommendationRepository.saveAll(newRecommendations);
    }
    
    private List<Recommendation> generateRecommendations(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get user preferences
        Optional<UserPreference> userPrefOpt = userPreferenceRepository.findByUserId(userId);
        
        // Generate popular recommendations
        recommendations.addAll(generatePopularRecommendations(userId, 5));
        
        // If user has preferences, generate personalized recommendations
        if (userPrefOpt.isPresent()) {
            UserPreference userPref = userPrefOpt.get();
            recommendations.addAll(generateContentBasedRecommendations(userId, userPref, 5));
        }
        
        // Limit to MAX_RECOMMENDATIONS
        return recommendations.stream()
            .limit(MAX_RECOMMENDATIONS)
            .collect(Collectors.toList());
    }
    
    private List<Recommendation> generateRecommendationsByType(Long userId, RecommendationType type) {
        return switch (type) {
            case POPULAR -> generatePopularRecommendations(userId, MAX_RECOMMENDATIONS);
            case TRENDING -> generateTrendingRecommendations(userId, MAX_RECOMMENDATIONS);
            case CONTENT_BASED -> {
                Optional<UserPreference> userPref = userPreferenceRepository.findByUserId(userId);
                yield userPref.map(pref -> generateContentBasedRecommendations(userId, pref, MAX_RECOMMENDATIONS))
                    .orElse(new ArrayList<>());
            }
            default -> new ArrayList<>();
        };
    }
    
    private List<Recommendation> generatePopularRecommendations(Long userId, int limit) {
        log.debug("Generating popular recommendations for user: {}", userId);
        
        List<BookAnalytics> popularBooks = bookAnalyticsRepository
            .findAllByOrderByPopularityScoreDesc(PageRequest.of(0, limit));
        
        LocalDateTime now = LocalDateTime.now();
        
        return popularBooks.stream()
            .map(analytics -> Recommendation.builder()
                .userId(userId)
                .bookId(analytics.getBookId())
                .score(analytics.getPopularityScore())
                .reason("Popular book with high borrowing rate")
                .type(RecommendationType.POPULAR)
                .createdAt(now)
                .expiresAt(now.plusDays(RECOMMENDATION_EXPIRY_DAYS))
                .active(true)
                .build())
            .collect(Collectors.toList());
    }
    
    private List<Recommendation> generateTrendingRecommendations(Long userId, int limit) {
        log.debug("Generating trending recommendations for user: {}", userId);
        
        List<BookAnalytics> trendingBooks = bookAnalyticsRepository
            .findAllByOrderByTotalBorrowsDesc(PageRequest.of(0, limit));
        
        LocalDateTime now = LocalDateTime.now();
        
        return trendingBooks.stream()
            .map(analytics -> Recommendation.builder()
                .userId(userId)
                .bookId(analytics.getBookId())
                .score((double) analytics.getTotalBorrows())
                .reason("Trending book with recent high activity")
                .type(RecommendationType.TRENDING)
                .createdAt(now)
                .expiresAt(now.plusDays(RECOMMENDATION_EXPIRY_DAYS))
                .active(true)
                .build())
            .collect(Collectors.toList());
    }
    
    private List<Recommendation> generateContentBasedRecommendations(
            Long userId, UserPreference userPref, int limit) {
        log.debug("Generating content-based recommendations for user: {}", userId);
        
        List<Recommendation> recommendations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Get all books
        List<BookResponse> allBooks = bookCatalogClient.getAllBooks();
        
        // Filter books based on user preferences
        List<BookResponse> matchingBooks = allBooks.stream()
            .filter(book -> !userPref.getBorrowedBooks().contains(book.getId()))
            .filter(book -> book.getAuthors() != null && book.getAuthors().stream()
                .anyMatch(author -> userPref.getFavoriteAuthors().contains(author.getId())))
            .limit(limit)
            .toList();
        
        for (BookResponse book : matchingBooks) {
            recommendations.add(Recommendation.builder()
                .userId(userId)
                .bookId(book.getId())
                .score(0.8)
                .reason("Based on your favorite authors")
                .type(RecommendationType.CONTENT_BASED)
                .createdAt(now)
                .expiresAt(now.plusDays(RECOMMENDATION_EXPIRY_DAYS))
                .active(true)
                .build());
        }
        
        return recommendations;
    }
    
    private List<RecommendationResponse> mapToResponseList(List<Recommendation> recommendations) {
        return recommendations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private RecommendationResponse mapToResponse(Recommendation recommendation) {
        RecommendationResponse response = RecommendationResponse.builder()
            .id(recommendation.getId())
            .userId(recommendation.getUserId())
            .bookId(recommendation.getBookId())
            .score(recommendation.getScore())
            .reason(recommendation.getReason())
            .type(recommendation.getType())
            .createdAt(recommendation.getCreatedAt())
            .build();
        
        // Fetch book info
        try {
            BookResponse book = bookCatalogClient.getBookById(recommendation.getBookId());
            if (book != null) {
                response.setBookInfo(RecommendationResponse.BookInfo.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .isbn(book.getIsbn())
                    .publicationYear(book.getPublicationYear())
                    .availableCopies(book.getAvailableCopies())
                    .build());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch book info for book: {}", recommendation.getBookId(), e);
        }
        
        return response;
    }
    
    @Transactional
    public void cleanupExpiredRecommendations() {
        log.info("Cleaning up expired recommendations");
        recommendationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
