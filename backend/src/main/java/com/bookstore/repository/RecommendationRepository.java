package com.bookstore.repository;

import com.bookstore.entity.Recommendation;
import com.bookstore.entity.RecommendationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "recommendations", path = "recommendations")
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    // Find recommendations by user ID
    Page<Recommendation> findByUserIdOrderByScoreDesc(String userId, Pageable pageable);
    
    // Find recommendations by user ID and type
    Page<Recommendation> findByUserIdAndTypeOrderByScoreDesc(String userId, RecommendationType type, Pageable pageable);
    
    // Find recommendations by type
    Page<Recommendation> findByTypeOrderByScoreDesc(RecommendationType type, Pageable pageable);
    
    // Find recommendations by book ID
    Page<Recommendation> findByBookId(Long bookId, Pageable pageable);
    
    // Find high-score recommendations (score >= threshold)
    @Query("SELECT r FROM Recommendation r WHERE r.score >= :threshold ORDER BY r.score DESC")
    Page<Recommendation> findHighScoreRecommendations(@Param("threshold") Double threshold, Pageable pageable);
    
    // Find recent recommendations for a user
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    Page<Recommendation> findRecentRecommendationsForUser(@Param("userId") String userId, 
                                                         @Param("since") LocalDateTime since, 
                                                         Pageable pageable);
    
    // Find top recommendations for a user (highest scores)
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId ORDER BY r.score DESC")
    Page<Recommendation> findTopRecommendationsForUser(@Param("userId") String userId, Pageable pageable);
    
    // Find popular recommendations (books recommended to many users)
    @Query("SELECT r.book.id, r.book.title, COUNT(r) as recommendationCount, AVG(r.score) as avgScore " +
           "FROM Recommendation r GROUP BY r.book.id, r.book.title ORDER BY COUNT(r) DESC")
    Page<Object[]> findPopularRecommendations(Pageable pageable);
    
    // Find recommendations by genre (through book relationship)
    @Query("SELECT r FROM Recommendation r WHERE LOWER(r.book.genre) = LOWER(:genre) ORDER BY r.score DESC")
    Page<Recommendation> findByBookGenre(@Param("genre") String genre, Pageable pageable);
    
    // Find recommendations for books by specific author
    @Query("SELECT r FROM Recommendation r JOIN r.book.authors a WHERE a.id = :authorId ORDER BY r.score DESC")
    Page<Recommendation> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    // Check if recommendation exists for user and book
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Recommendation r " +
           "WHERE r.userId = :userId AND r.book.id = :bookId")
    boolean existsByUserIdAndBookId(@Param("userId") String userId, @Param("bookId") Long bookId);
    
    // Find existing recommendation for user and book
    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.book.id = :bookId")
    List<Recommendation> findByUserIdAndBookId(@Param("userId") String userId, @Param("bookId") Long bookId);
    
    // Delete old recommendations for a user (keep only recent ones)
    @Query("DELETE FROM Recommendation r WHERE r.userId = :userId AND r.createdAt < :cutoffDate")
    void deleteOldRecommendationsForUser(@Param("userId") String userId, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find recommendations by score range
    @Query("SELECT r FROM Recommendation r WHERE r.score BETWEEN :minScore AND :maxScore ORDER BY r.score DESC")
    Page<Recommendation> findByScoreRange(@Param("minScore") Double minScore, 
                                         @Param("maxScore") Double maxScore, 
                                         Pageable pageable);
    
    // Count recommendations by type
    Long countByType(RecommendationType type);
    
    // Count recommendations for a user
    Long countByUserId(String userId);
    
    // Find trending recommendations (recent with high scores)
    @Query("SELECT r FROM Recommendation r WHERE r.createdAt >= :since AND r.score >= :minScore " +
           "ORDER BY r.score DESC, r.createdAt DESC")
    Page<Recommendation> findTrendingRecommendations(@Param("since") LocalDateTime since, 
                                                    @Param("minScore") Double minScore, 
                                                    Pageable pageable);
    
    // Find collaborative filtering recommendations for similar users
    @Query("SELECT r FROM Recommendation r WHERE r.type = 'COLLABORATIVE' AND r.userId != :userId " +
           "AND r.book.id IN (SELECT r2.book.id FROM Recommendation r2 WHERE r2.userId = :userId) " +
           "ORDER BY r.score DESC")
    Page<Recommendation> findCollaborativeRecommendations(@Param("userId") String userId, Pageable pageable);
    
    // Find content-based recommendations by genre preference
    @Query("SELECT r FROM Recommendation r WHERE r.type = 'CONTENT_BASED' AND r.userId = :userId " +
           "AND r.book.genre IN :preferredGenres ORDER BY r.score DESC")
    Page<Recommendation> findContentBasedRecommendationsByGenres(@Param("userId") String userId, 
                                                               @Param("preferredGenres") List<String> preferredGenres, 
                                                               Pageable pageable);
}