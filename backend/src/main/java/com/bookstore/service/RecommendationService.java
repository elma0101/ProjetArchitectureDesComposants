package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AuthorRepository authorRepository;
    
    // Configuration constants
    private static final int DEFAULT_RECOMMENDATION_LIMIT = 10;
    private static final double MIN_SCORE_THRESHOLD = 0.1;
    private static final double POPULAR_BOOK_THRESHOLD = 0.7;
    private static final int TRENDING_DAYS_LOOKBACK = 30;
    private static final int COLLABORATIVE_USER_SIMILARITY_THRESHOLD = 2;
    
    @Autowired
    public RecommendationService(RecommendationRepository recommendationRepository,
                               BookRepository bookRepository,
                               LoanRepository loanRepository,
                               AuthorRepository authorRepository) {
        this.recommendationRepository = recommendationRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.authorRepository = authorRepository;
    }
    
    /**
     * Generate comprehensive recommendations for a user
     */
    @Cacheable(value = "recommendations", key = "#userId + '_' + T(java.lang.String).valueOf(#root.target.DEFAULT_RECOMMENDATION_LIMIT)")
    public List<Recommendation> generateRecommendationsForUser(String userId) {
        return generateRecommendationsForUser(userId, DEFAULT_RECOMMENDATION_LIMIT);
    }
    
    /**
     * Generate comprehensive recommendations for a user with specified limit
     */
    @Cacheable(value = "recommendations", key = "#userId + '_' + #limit")
    public List<Recommendation> generateRecommendationsForUser(String userId, int limit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get user's borrowing history
        List<Loan> userLoans = getUserLoanHistory(userId);
        
        if (userLoans.isEmpty()) {
            // New user - provide popular and trending recommendations
            recommendations.addAll(generatePopularRecommendations(limit / 2));
            recommendations.addAll(generateTrendingRecommendations(limit / 2));
        } else {
            // Existing user - use collaborative and content-based filtering
            recommendations.addAll(generateCollaborativeFilteringRecommendations(userId, limit / 3));
            recommendations.addAll(generateContentBasedRecommendations(userId, limit / 3));
            recommendations.addAll(generatePopularRecommendations(limit / 3));
        }
        
        // Remove duplicates and sort by score
        Map<Long, Recommendation> uniqueRecommendations = new HashMap<>();
        for (Recommendation rec : recommendations) {
            Long bookId = rec.getBook().getId();
            if (!uniqueRecommendations.containsKey(bookId) || 
                uniqueRecommendations.get(bookId).getScore() < rec.getScore()) {
                uniqueRecommendations.put(bookId, rec);
            }
        }
        
        return uniqueRecommendations.values().stream()
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate collaborative filtering recommendations
     */
    public List<Recommendation> generateCollaborativeFilteringRecommendations(String userId, int limit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get user's borrowed books
        Set<Long> userBookIds = getUserBorrowedBookIds(userId);
        if (userBookIds.isEmpty()) {
            return recommendations;
        }
        
        // Find similar users (users who borrowed similar books)
        Map<String, Integer> similarUsers = findSimilarUsers(userId, userBookIds);
        
        // Get books borrowed by similar users that current user hasn't borrowed
        Map<Long, Double> bookScores = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : similarUsers.entrySet()) {
            String similarUserId = entry.getKey();
            int similarity = entry.getValue();
            
            Set<Long> similarUserBooks = getUserBorrowedBookIds(similarUserId);
            for (Long bookId : similarUserBooks) {
                if (!userBookIds.contains(bookId)) {
                    double score = calculateCollaborativeScore(similarity, similarUserBooks.size());
                    bookScores.merge(bookId, score, Double::sum);
                }
            }
        }
        
        // Create recommendations from top scored books
        List<Map.Entry<Long, Double>> sortedBooks = bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        for (Map.Entry<Long, Double> entry : sortedBooks) {
            Optional<Book> book = bookRepository.findById(entry.getKey());
            if (book.isPresent()) {
                Recommendation recommendation = new Recommendation(
                    userId, 
                    book.get(), 
                    RecommendationType.COLLABORATIVE, 
                    Math.min(entry.getValue(), 1.0)
                );
                recommendation.setReason("Users with similar reading preferences also borrowed this book");
                recommendations.add(recommendation);
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate content-based recommendations using book genres and authors
     */
    public List<Recommendation> generateContentBasedRecommendations(String userId, int limit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get user's reading preferences from loan history
        UserPreferences preferences = analyzeUserPreferences(userId);
        if (preferences.isEmpty()) {
            return recommendations;
        }
        
        // Get user's already borrowed books to exclude them
        Set<Long> userBookIds = getUserBorrowedBookIds(userId);
        
        // Find books matching user preferences
        Map<Long, Double> bookScores = new HashMap<>();
        
        // Score books by genre preference
        for (Map.Entry<String, Double> genreEntry : preferences.getGenrePreferences().entrySet()) {
            String genre = genreEntry.getKey();
            double genreWeight = genreEntry.getValue();
            
            List<Book> genreBooks = bookRepository.findByGenreIgnoreCase(genre);
            for (Book book : genreBooks) {
                if (!userBookIds.contains(book.getId())) {
                    double score = genreWeight * 0.6; // Genre contributes 60% to content score
                    bookScores.merge(book.getId(), score, Double::sum);
                }
            }
        }
        
        // Score books by author preference
        for (Map.Entry<Long, Double> authorEntry : preferences.getAuthorPreferences().entrySet()) {
            Long authorId = authorEntry.getKey();
            double authorWeight = authorEntry.getValue();
            
            Optional<Author> author = authorRepository.findById(authorId);
            if (author.isPresent()) {
                for (Book book : author.get().getBooks()) {
                    if (!userBookIds.contains(book.getId())) {
                        double score = authorWeight * 0.4; // Author contributes 40% to content score
                        bookScores.merge(book.getId(), score, Double::sum);
                    }
                }
            }
        }
        
        // Create recommendations from top scored books
        List<Map.Entry<Long, Double>> sortedBooks = bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        for (Map.Entry<Long, Double> entry : sortedBooks) {
            Optional<Book> book = bookRepository.findById(entry.getKey());
            if (book.isPresent()) {
                Recommendation recommendation = new Recommendation(
                    userId, 
                    book.get(), 
                    RecommendationType.CONTENT_BASED, 
                    Math.min(entry.getValue(), 1.0)
                );
                recommendation.setReason("Based on your reading preferences for genres and authors");
                recommendations.add(recommendation);
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate popular book recommendations
     */
    @Cacheable(value = "popularBooks", key = "#limit")
    public List<Recommendation> generatePopularRecommendations(int limit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get most borrowed books
        Page<Object[]> popularBooks = loanRepository.findMostBorrowedBooks(PageRequest.of(0, limit * 2));
        
        for (Object[] result : popularBooks.getContent()) {
            Long bookId = (Long) result[0];
            String title = (String) result[1];
            Long loanCount = (Long) result[2];
            
            Optional<Book> book = bookRepository.findById(bookId);
            if (book.isPresent()) {
                // Calculate popularity score based on loan count
                double score = Math.min(loanCount / 10.0, 1.0); // Normalize to 0-1 scale
                
                if (score >= MIN_SCORE_THRESHOLD) {
                    Recommendation recommendation = new Recommendation(
                        null, // Popular recommendations are not user-specific
                        book.get(), 
                        RecommendationType.POPULAR, 
                        score
                    );
                    recommendation.setReason("Popular book borrowed " + loanCount + " times");
                    recommendations.add(recommendation);
                }
            }
            
            if (recommendations.size() >= limit) {
                break;
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate trending book recommendations
     */
    @Cacheable(value = "popularBooks", key = "'trending_' + #limit")
    public List<Recommendation> generateTrendingRecommendations(int limit) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Get recent loans (last 30 days)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(TRENDING_DAYS_LOOKBACK);
        
        // Count recent loans per book
        Map<Long, Integer> recentLoanCounts = new HashMap<>();
        Page<Loan> recentLoans = loanRepository.findRecentLoans(PageRequest.of(0, 1000));
        
        for (Loan loan : recentLoans.getContent()) {
            if (loan.getCreatedAt().isAfter(cutoffDate)) {
                recentLoanCounts.merge(loan.getBook().getId(), 1, Integer::sum);
            }
        }
        
        // Sort by recent loan count and create recommendations
        List<Map.Entry<Long, Integer>> sortedBooks = recentLoanCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        for (Map.Entry<Long, Integer> entry : sortedBooks) {
            Optional<Book> book = bookRepository.findById(entry.getKey());
            if (book.isPresent()) {
                int recentLoanCount = entry.getValue();
                double score = Math.min(recentLoanCount / 5.0, 1.0); // Normalize to 0-1 scale
                
                if (score >= MIN_SCORE_THRESHOLD) {
                    Recommendation recommendation = new Recommendation(
                        null, // Trending recommendations are not user-specific
                        book.get(), 
                        RecommendationType.TRENDING, 
                        score
                    );
                    recommendation.setReason("Trending book with " + recentLoanCount + " recent loans");
                    recommendations.add(recommendation);
                }
            }
        }
        
        return recommendations;
    }
    
    /**
     * Save recommendations to database
     */
    public List<Recommendation> saveRecommendations(List<Recommendation> recommendations) {
        List<Recommendation> savedRecommendations = new ArrayList<>();
        
        for (Recommendation recommendation : recommendations) {
            // Check if recommendation already exists
            if (recommendation.getUserId() != null) {
                List<Recommendation> existing = recommendationRepository
                    .findByUserIdAndBookId(recommendation.getUserId(), recommendation.getBook().getId());
                
                if (!existing.isEmpty()) {
                    // Update existing recommendation if new score is higher
                    Recommendation existingRec = existing.get(0);
                    if (recommendation.getScore() > existingRec.getScore()) {
                        existingRec.setScore(recommendation.getScore());
                        existingRec.setReason(recommendation.getReason());
                        existingRec.setType(recommendation.getType());
                        savedRecommendations.add(recommendationRepository.save(existingRec));
                    } else {
                        savedRecommendations.add(existingRec);
                    }
                } else {
                    savedRecommendations.add(recommendationRepository.save(recommendation));
                }
            } else {
                savedRecommendations.add(recommendationRepository.save(recommendation));
            }
        }
        
        return savedRecommendations;
    }
    
    /**
     * Get recommendations for a user from database
     */
    public Page<Recommendation> getRecommendationsForUser(String userId, Pageable pageable) {
        return recommendationRepository.findByUserIdOrderByScoreDesc(userId, pageable);
    }
    
    /**
     * Get recommendations by type
     */
    public Page<Recommendation> getRecommendationsByType(RecommendationType type, Pageable pageable) {
        return recommendationRepository.findByTypeOrderByScoreDesc(type, pageable);
    }
    
    /**
     * Clean up old recommendations for a user
     */
    public void cleanupOldRecommendations(String userId, int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        recommendationRepository.deleteOldRecommendationsForUser(userId, cutoffDate);
    }
    
    // Helper methods
    
    private List<Loan> getUserLoanHistory(String userId) {
        if (userId == null) return new ArrayList<>();
        return loanRepository.findByBorrowerId(userId, Pageable.unpaged()).getContent();
    }
    
    private Set<Long> getUserBorrowedBookIds(String userId) {
        return getUserLoanHistory(userId).stream()
                .map(loan -> loan.getBook().getId())
                .collect(Collectors.toSet());
    }
    
    private Map<String, Integer> findSimilarUsers(String userId, Set<Long> userBookIds) {
        Map<String, Integer> similarUsers = new HashMap<>();
        
        // Get all loans and group by borrower
        Map<String, Set<Long>> allUserBooks = new HashMap<>();
        Page<Loan> allLoans = loanRepository.findAll(Pageable.unpaged());
        
        for (Loan loan : allLoans.getContent()) {
            String borrowerId = loan.getBorrowerId();
            if (borrowerId != null && !borrowerId.equals(userId)) {
                allUserBooks.computeIfAbsent(borrowerId, k -> new HashSet<>())
                          .add(loan.getBook().getId());
            }
        }
        
        // Calculate similarity (number of common books)
        for (Map.Entry<String, Set<Long>> entry : allUserBooks.entrySet()) {
            String otherUserId = entry.getKey();
            Set<Long> otherUserBooks = entry.getValue();
            
            Set<Long> commonBooks = new HashSet<>(userBookIds);
            commonBooks.retainAll(otherUserBooks);
            
            if (commonBooks.size() >= COLLABORATIVE_USER_SIMILARITY_THRESHOLD) {
                similarUsers.put(otherUserId, commonBooks.size());
            }
        }
        
        return similarUsers;
    }
    
    private double calculateCollaborativeScore(int similarity, int totalBooks) {
        // Score based on similarity and diversity of the similar user's reading
        return Math.min((similarity * 0.7) + (totalBooks * 0.01), 1.0);
    }
    
    private UserPreferences analyzeUserPreferences(String userId) {
        List<Loan> userLoans = getUserLoanHistory(userId);
        UserPreferences preferences = new UserPreferences();
        
        Map<String, Integer> genreCounts = new HashMap<>();
        Map<Long, Integer> authorCounts = new HashMap<>();
        
        for (Loan loan : userLoans) {
            Book book = loan.getBook();
            
            // Count genres
            if (book.getGenre() != null) {
                genreCounts.merge(book.getGenre(), 1, Integer::sum);
            }
            
            // Count authors
            for (Author author : book.getAuthors()) {
                authorCounts.merge(author.getId(), 1, Integer::sum);
            }
        }
        
        // Convert counts to preferences (normalized scores)
        int totalLoans = userLoans.size();
        if (totalLoans > 0) {
            for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
                double preference = (double) entry.getValue() / totalLoans;
                preferences.addGenrePreference(entry.getKey(), preference);
            }
            
            for (Map.Entry<Long, Integer> entry : authorCounts.entrySet()) {
                double preference = (double) entry.getValue() / totalLoans;
                preferences.addAuthorPreference(entry.getKey(), preference);
            }
        }
        
        return preferences;
    }
    
    // Inner class for user preferences
    private static class UserPreferences {
        private final Map<String, Double> genrePreferences = new HashMap<>();
        private final Map<Long, Double> authorPreferences = new HashMap<>();
        
        public void addGenrePreference(String genre, double preference) {
            genrePreferences.put(genre, preference);
        }
        
        public void addAuthorPreference(Long authorId, double preference) {
            authorPreferences.put(authorId, preference);
        }
        
        public Map<String, Double> getGenrePreferences() {
            return genrePreferences;
        }
        
        public Map<Long, Double> getAuthorPreferences() {
            return authorPreferences;
        }
        
        public boolean isEmpty() {
            return genrePreferences.isEmpty() && authorPreferences.isEmpty();
        }
    }
}