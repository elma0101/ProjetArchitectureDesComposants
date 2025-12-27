package com.bookstore.recommendation.repository;

import com.bookstore.recommendation.entity.BookAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookAnalyticsRepository extends MongoRepository<BookAnalytics, String> {
    
    Optional<BookAnalytics> findByBookId(Long bookId);
    
    List<BookAnalytics> findAllByOrderByPopularityScoreDesc(Pageable pageable);
    
    List<BookAnalytics> findAllByOrderByTotalBorrowsDesc(Pageable pageable);
    
    List<BookAnalytics> findAllByOrderByAverageRatingDesc(Pageable pageable);
}
