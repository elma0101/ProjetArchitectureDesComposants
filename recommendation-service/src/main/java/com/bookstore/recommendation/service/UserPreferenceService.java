package com.bookstore.recommendation.service;

import com.bookstore.recommendation.dto.FeedbackRequest;
import com.bookstore.recommendation.dto.UserPreferenceResponse;
import com.bookstore.recommendation.entity.UserPreference;
import com.bookstore.recommendation.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    
    private final UserPreferenceRepository userPreferenceRepository;
    
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreference(Long userId) {
        log.info("Fetching user preference for user: {}", userId);
        
        UserPreference userPref = userPreferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        
        return mapToResponse(userPref);
    }
    
    @Transactional
    public UserPreferenceResponse recordFeedback(FeedbackRequest request) {
        log.info("Recording feedback for user: {}, book: {}, rating: {}", 
            request.getUserId(), request.getBookId(), request.getRating());
        
        UserPreference userPref = userPreferenceRepository.findByUserId(request.getUserId())
            .orElseGet(() -> createDefaultPreference(request.getUserId()));
        
        // Update rating
        userPref.getBookRatings().put(request.getBookId(), request.getRating());
        userPref.setLastUpdated(LocalDateTime.now());
        
        userPref = userPreferenceRepository.save(userPref);
        
        return mapToResponse(userPref);
    }
    
    @Transactional
    public UserPreferenceResponse recordBorrow(Long userId, Long bookId) {
        log.info("Recording borrow for user: {}, book: {}", userId, bookId);
        
        UserPreference userPref = userPreferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        
        // Add to borrowed books if not already present
        if (!userPref.getBorrowedBooks().contains(bookId)) {
            userPref.getBorrowedBooks().add(bookId);
            userPref.setTotalBorrows(userPref.getTotalBorrows() + 1);
        }
        
        userPref.setLastUpdated(LocalDateTime.now());
        userPref = userPreferenceRepository.save(userPref);
        
        return mapToResponse(userPref);
    }
    
    @Transactional
    public UserPreferenceResponse addFavoriteAuthor(Long userId, Long authorId) {
        log.info("Adding favorite author {} for user: {}", authorId, userId);
        
        UserPreference userPref = userPreferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        
        if (!userPref.getFavoriteAuthors().contains(authorId)) {
            userPref.getFavoriteAuthors().add(authorId);
            userPref.setLastUpdated(LocalDateTime.now());
            userPref = userPreferenceRepository.save(userPref);
        }
        
        return mapToResponse(userPref);
    }
    
    @Transactional
    public UserPreferenceResponse addFavoriteCategory(Long userId, String category) {
        log.info("Adding favorite category {} for user: {}", category, userId);
        
        UserPreference userPref = userPreferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreference(userId));
        
        if (!userPref.getFavoriteCategories().contains(category)) {
            userPref.getFavoriteCategories().add(category);
            userPref.setLastUpdated(LocalDateTime.now());
            userPref = userPreferenceRepository.save(userPref);
        }
        
        return mapToResponse(userPref);
    }
    
    private UserPreference createDefaultPreference(Long userId) {
        return UserPreference.builder()
            .userId(userId)
            .favoriteCategories(new ArrayList<>())
            .favoriteAuthors(new ArrayList<>())
            .borrowedBooks(new ArrayList<>())
            .lastUpdated(LocalDateTime.now())
            .totalBorrows(0)
            .build();
    }
    
    private UserPreferenceResponse mapToResponse(UserPreference userPref) {
        return UserPreferenceResponse.builder()
            .id(userPref.getId())
            .userId(userPref.getUserId())
            .favoriteCategories(userPref.getFavoriteCategories())
            .favoriteAuthors(userPref.getFavoriteAuthors())
            .borrowedBooks(userPref.getBorrowedBooks())
            .bookRatings(userPref.getBookRatings())
            .lastUpdated(userPref.getLastUpdated())
            .totalBorrows(userPref.getTotalBorrows())
            .build();
    }
}
