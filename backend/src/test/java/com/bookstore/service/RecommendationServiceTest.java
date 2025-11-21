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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    
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
    
    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Author testAuthor1;
    private Author testAuthor2;
    private Loan testLoan1;
    private Loan testLoan2;
    private Recommendation testRecommendation;
    
    @BeforeEach
    void setUp() {
        // Create test authors
        testAuthor1 = new Author();
        testAuthor1.setId(1L);
        testAuthor1.setFirstName("John");
        testAuthor1.setLastName("Doe");
        
        testAuthor2 = new Author();
        testAuthor2.setId(2L);
        testAuthor2.setFirstName("Jane");
        testAuthor2.setLastName("Smith");
        
        // Create test books
        testBook1 = new Book();
        testBook1.setId(1L);
        testBook1.setTitle("Test Book 1");
        testBook1.setIsbn("978-0123456789");
        testBook1.setGenre("Fiction");
        testBook1.setAvailableCopies(5);
        testBook1.setTotalCopies(10);
        testBook1.getAuthors().add(testAuthor1);
        
        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("Test Book 2");
        testBook2.setIsbn("978-0123456790");
        testBook2.setGenre("Fiction");
        testBook2.setAvailableCopies(3);
        testBook2.setTotalCopies(5);
        testBook2.getAuthors().add(testAuthor1);
        
        testBook3 = new Book();
        testBook3.setId(3L);
        testBook3.setTitle("Test Book 3");
        testBook3.setIsbn("978-0123456791");
        testBook3.setGenre("Science");
        testBook3.setAvailableCopies(2);
        testBook3.setTotalCopies(3);
        testBook3.getAuthors().add(testAuthor2);
        
        // Create test loans
        testLoan1 = new Loan();
        testLoan1.setId(1L);
        testLoan1.setBook(testBook1);
        testLoan1.setBorrowerId("user1");
        testLoan1.setBorrowerName("Test User 1");
        testLoan1.setBorrowerEmail("user1@test.com");
        testLoan1.setLoanDate(LocalDate.now().minusDays(10));
        testLoan1.setDueDate(LocalDate.now().plusDays(4));
        testLoan1.setStatus(LoanStatus.ACTIVE);
        
        // Set createdAt using reflection
        try {
            java.lang.reflect.Field createdAtField = Loan.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testLoan1, LocalDateTime.now().minusDays(10));
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
        
        testLoan2 = new Loan();
        testLoan2.setId(2L);
        testLoan2.setBook(testBook2);
        testLoan2.setBorrowerId("user2");
        testLoan2.setBorrowerName("Test User 2");
        testLoan2.setBorrowerEmail("user2@test.com");
        testLoan2.setLoanDate(LocalDate.now().minusDays(5));
        testLoan2.setDueDate(LocalDate.now().plusDays(9));
        testLoan2.setStatus(LoanStatus.ACTIVE);
        
        // Set createdAt using reflection
        try {
            java.lang.reflect.Field createdAtField = Loan.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testLoan2, LocalDateTime.now().minusDays(5));
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
        
        // Create test recommendation
        testRecommendation = new Recommendation();
        testRecommendation.setId(1L);
        testRecommendation.setUserId("user1");
        testRecommendation.setBook(testBook2);
        testRecommendation.setType(RecommendationType.COLLABORATIVE);
        testRecommendation.setScore(0.8);
        testRecommendation.setReason("Test recommendation");
    }
    
    @Test
    void testGenerateRecommendationsForNewUser() {
        // Given
        String userId = "newuser";
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        
        // Mock popular books
        Object[] popularBookResult = {1L, "Popular Book", 10L};
        List<Object[]> popularBooksList = new ArrayList<>();
        popularBooksList.add(popularBookResult);
        Page<Object[]> popularBooks = new PageImpl<>(popularBooksList);
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(popularBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook1));
        
        // Mock recent loans for trending
        when(loanRepository.findRecentLoans(any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(testLoan1)));
        
        // When
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId);
        
        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        verify(loanRepository).findByBorrowerId(eq(userId), any(Pageable.class));
        verify(loanRepository).findMostBorrowedBooks(any(PageRequest.class));
    }
    
    @Test
    void testGenerateRecommendationsForExistingUser() {
        // Given
        String userId = "user1";
        List<Loan> userLoans = List.of(testLoan1);
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(userLoans));
        
        // Mock all loans for collaborative filtering
        when(loanRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testLoan1, testLoan2)));
        
        // Mock books by genre for content-based filtering
        when(bookRepository.findByGenreIgnoreCase("Fiction"))
            .thenReturn(List.of(testBook2, testBook3));
        
        // Mock author repository
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor1));
        
        // Mock popular books
        Object[] popularBookResult = {2L, "Popular Book", 5L};
        List<Object[]> popularBooksList = new ArrayList<>();
        popularBooksList.add(popularBookResult);
        Page<Object[]> popularBooks = new PageImpl<>(popularBooksList);
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(popularBooks);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook2));
        
        // When
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId);
        
        // Then
        assertNotNull(recommendations);
        verify(loanRepository).findByBorrowerId(eq(userId), any(Pageable.class));
        verify(bookRepository).findByGenreIgnoreCase("Fiction");
    }
    
    @Test
    void testGenerateCollaborativeFilteringRecommendations() {
        // Given
        String userId = "user1";
        List<Loan> userLoans = List.of(testLoan1);
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(userLoans));
        
        // Create similar user loan
        Loan similarUserLoan = new Loan();
        similarUserLoan.setBook(testBook1); // Same book as user1
        similarUserLoan.setBorrowerId("user2");
        
        Loan similarUserLoan2 = new Loan();
        similarUserLoan2.setBook(testBook2); // Different book
        similarUserLoan2.setBorrowerId("user2");
        
        when(loanRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testLoan1, similarUserLoan, similarUserLoan2)));
        
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook2));
        
        // When
        List<Recommendation> recommendations = recommendationService
            .generateCollaborativeFilteringRecommendations(userId, 5);
        
        // Then
        assertNotNull(recommendations);
        verify(loanRepository).findByBorrowerId(eq(userId), any(Pageable.class));
        verify(loanRepository).findAll(any(Pageable.class));
    }
    
    @Test
    void testGenerateContentBasedRecommendations() {
        // Given
        String userId = "user1";
        List<Loan> userLoans = List.of(testLoan1); // User borrowed Fiction book
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(userLoans));
        
        // Mock books by genre
        when(bookRepository.findByGenreIgnoreCase("Fiction"))
            .thenReturn(List.of(testBook2)); // Another Fiction book
        
        // Mock author books
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor1));
        testAuthor1.getBooks().add(testBook2);
        
        // When
        List<Recommendation> recommendations = recommendationService
            .generateContentBasedRecommendations(userId, 5);
        
        // Then
        assertNotNull(recommendations);
        verify(bookRepository).findByGenreIgnoreCase("Fiction");
        verify(authorRepository).findById(1L);
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
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook2));
        
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
    void testGenerateTrendingRecommendations() {
        // Given
        // Create loans with specific creation times using reflection
        Loan recentLoan1 = new Loan();
        recentLoan1.setId(1L);
        recentLoan1.setBook(testBook1);
        recentLoan1.setBorrowerId("user1");
        
        Loan recentLoan2 = new Loan();
        recentLoan2.setId(2L);
        recentLoan2.setBook(testBook2);
        recentLoan2.setBorrowerId("user2");
        
        // Set creation times using reflection
        try {
            java.lang.reflect.Field createdAtField = Loan.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(recentLoan1, LocalDateTime.now().minusDays(5));
            createdAtField.set(recentLoan2, LocalDateTime.now().minusDays(2));
        } catch (Exception e) {
            // Fallback - just use the loans as they are
        }
        
        Page<Loan> recentLoans = new PageImpl<>(List.of(recentLoan1, recentLoan2));
        when(loanRepository.findRecentLoans(any(PageRequest.class)))
            .thenReturn(recentLoans);
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook2));
        
        // When
        List<Recommendation> recommendations = recommendationService.generateTrendingRecommendations(5);
        
        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(RecommendationType.TRENDING, recommendations.get(0).getType());
        assertTrue(recommendations.get(0).getScore() > 0);
        verify(loanRepository).findRecentLoans(any(PageRequest.class));
    }
    
    @Test
    void testSaveRecommendations() {
        // Given
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 2L))
            .thenReturn(Collections.emptyList());
        when(recommendationRepository.save(any(Recommendation.class)))
            .thenReturn(testRecommendation);
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        verify(recommendationRepository).findByUserIdAndBookId("user1", 2L);
        verify(recommendationRepository).save(any(Recommendation.class));
    }
    
    @Test
    void testSaveRecommendationsWithExistingHigherScore() {
        // Given
        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setScore(0.9); // Higher than test recommendation (0.8)
        
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 2L))
            .thenReturn(List.of(existingRecommendation));
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        assertEquals(existingRecommendation, savedRecommendations.get(0));
        verify(recommendationRepository).findByUserIdAndBookId("user1", 2L);
        verify(recommendationRepository, never()).save(any(Recommendation.class));
    }
    
    @Test
    void testSaveRecommendationsWithExistingLowerScore() {
        // Given
        Recommendation existingRecommendation = new Recommendation();
        existingRecommendation.setScore(0.5); // Lower than test recommendation (0.8)
        
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationRepository.findByUserIdAndBookId("user1", 2L))
            .thenReturn(List.of(existingRecommendation));
        when(recommendationRepository.save(any(Recommendation.class)))
            .thenReturn(existingRecommendation);
        
        // When
        List<Recommendation> savedRecommendations = recommendationService.saveRecommendations(recommendations);
        
        // Then
        assertNotNull(savedRecommendations);
        assertEquals(1, savedRecommendations.size());
        verify(recommendationRepository).findByUserIdAndBookId("user1", 2L);
        verify(recommendationRepository).save(existingRecommendation);
        assertEquals(0.8, existingRecommendation.getScore());
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
    void testGenerateRecommendationsForUserWithLimit() {
        // Given
        String userId = "user1";
        int limit = 5;
        List<Loan> userLoans = List.of(testLoan1);
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(userLoans));
        
        // Mock collaborative filtering
        when(loanRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testLoan1, testLoan2)));
        
        // Mock content-based filtering
        when(bookRepository.findByGenreIgnoreCase("Fiction"))
            .thenReturn(List.of(testBook2));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor1));
        
        // Mock popular books
        Object[] popularBookResult = {3L, "Popular Book", 8L};
        List<Object[]> popularBooksList = new ArrayList<>();
        popularBooksList.add(popularBookResult);
        Page<Object[]> popularBooks = new PageImpl<>(popularBooksList);
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(popularBooks);
        when(bookRepository.findById(3L)).thenReturn(Optional.of(testBook3));
        
        // When
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId, limit);
        
        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= limit);
    }
    
    @Test
    void testGenerateRecommendationsRemovesDuplicates() {
        // Given
        String userId = "user1";
        List<Loan> userLoans = List.of(testLoan1);
        when(loanRepository.findByBorrowerId(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(userLoans));
        
        // Mock to return same book in different recommendation types
        when(loanRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testLoan1, testLoan2)));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook2));
        
        when(bookRepository.findByGenreIgnoreCase("Fiction"))
            .thenReturn(List.of(testBook2)); // Same book as collaborative
        
        Object[] popularBookResult = {2L, "Same Book", 8L}; // Same book again
        List<Object[]> popularBooksList = new ArrayList<>();
        popularBooksList.add(popularBookResult);
        Page<Object[]> popularBooks = new PageImpl<>(popularBooksList);
        when(loanRepository.findMostBorrowedBooks(any(PageRequest.class)))
            .thenReturn(popularBooks);
        
        // When
        List<Recommendation> recommendations = recommendationService.generateRecommendationsForUser(userId, 10);
        
        // Then
        assertNotNull(recommendations);
        // Should have only one recommendation for testBook2 despite appearing in multiple algorithms
        long book2Count = recommendations.stream()
            .filter(r -> r.getBook().getId().equals(2L))
            .count();
        assertEquals(1, book2Count);
    }
}