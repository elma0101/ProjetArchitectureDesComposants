package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceSimpleTest {
    
    @Mock
    private RecommendationRepository recommendationRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private AuthorRepository authorRepository;
    
    @InjectMocks
    private RecommendationService recommendationService;
    
    private Book testBook;
    private Author testAuthor;
    private Recommendation testRecommendation;
    
    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        
        // Create test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setGenre("Fiction");
        testBook.setAvailableCopies(5);
        testBook.setTotalCopies(10);
        testBook.getAuthors().add(testAuthor);
        
        // Create test recommendation
        testRecommendation = new Recommendation();
        testRecommendation.setId(1L);
        testRecommendation.setUserId("user1");
        testRecommendation.setBook(testBook);
        testRecommendation.setType(RecommendationType.COLLABORATIVE);
        testRecommendation.setScore(0.8);
        testRecommendation.setReason("Test recommendation");
    }
    
    @Test
    void testGeneratePopularRecommendations() {
        // Given
        Object[] popularBookResult1 = {1L, "Popular Book 1", 15L};
        Object[] popularBookResult2 = {2L, "Popular Book 2", 10L};
        List<Object[]> popularBooksList = new ArrayList<>();
        popularBooksList.add(popularBookResult1);
        popularBooksList.add(popularBookResult2);
        Page<Object[]> popularBooks = new PageImpl<>(popularBooksList);
        
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(popularBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook));
        
        // When
        List<Recommendation> recommendations = recommendationService.generatePopularRecommendations(5);
        
        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(2, recommendations.size());
        assertEquals(RecommendationType.POPULAR, recommendations.get(0).getType());
        assertTrue(recommendations.get(0).getScore() > 0);
        verify(loanRepository).findMostBorrowedBooks(any(PageRequest.class));
    }
    
    @Test
    void testSaveRecommendations() {
        // Given
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 1L))
            .thenReturn(Collections.emptyList());
        when(recommendationRepository.save(any(Recommendation.class)))
            .thenReturn(testRecommendation);
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        verify(recommendationRepository).findByUserIdAndBookId("user1", 1L);
        verify(recommendationRepository).save(any(Recommendation.class));
    }
    
    @Test
    void testGetRecommendationsForUser() {
        // Given
        String userId = "user1";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> expectedPage = new PageImpl<>(List.of(testRecommendation));
        
        when(recommendationRepository.findByUserIdOrderByScoreDesc(userId, pageable))
            .thenReturn(expectedPage);
        
        // When
        Page<Recommendation> result = recommendationService.getRecommendationsForUser(userId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(recommendationRepository).findByUserIdOrderByScoreDesc(userId, pageable);
    }
    
    @Test
    void testGetRecommendationsByType() {
        // Given
        RecommendationType type = RecommendationType.COLLABORATIVE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> expectedPage = new PageImpl<>(List.of(testRecommendation));
        
        when(recommendationRepository.findByTypeOrderByScoreDesc(type, pageable))
            .thenReturn(expectedPage);
        
        // When
        Page<Recommendation> result = recommendationService.getRecommendationsByType(type, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(recommendationRepository).findByTypeOrderByScoreDesc(type, pageable);
    }
    
    @Test
    void testCleanupOldRecommendations() {
        // Given
        String userId = "user1";
        int daysToKeep = 30;
        
        // When
        recommendationService.cleanupOldRecommendations(userId, daysToKeep);
        
        // Then
        verify(recommendationRepository).deleteOldRecommendationsForUser(eq(userId), any(LocalDateTime.class));
    }
    
    @Test
    void testSaveRecommendationsWithExistingHigherScore() {
        // Given
        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setScore(0.9); // Higher than test recommendation (0.8)
        
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 1L))
            .thenReturn(List.of(existingRecommendation));
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        assertEquals(existingRecommendation, savedRecommendations.get(0));
        verify(recommendationRepository).findByUserIdAndBookId("user1", 1L);
        verify(recommendationRepository, never()).save(any(Recommendation.class));
    }
    
    @Test
    void testSaveRecommendationsWithExistingLowerScore() {
        // Given
        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setScore(0.5); // Lower than test recommendation (0.8)
        
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 1L))
            .thenReturn(List.of(existingRecommendation));
        when(recommendationRepository.save(any(Recommendation.class)))
            .thenReturn(existingRecommendation);
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        verify(recommendationRepository).findByUserIdAndBookId("user1", 1L);
        verify(recommendationRepository).save(existingRecommendation);
        assertEquals(0.8, existingRecommendation.getScore());
    }
}