package com.bookstore.controller;

import com.bookstore.entity.*;
import com.bookstore.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@Import(com.bookstore.config.TestSecurityConfig.class)
class RecommendationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RecommendationService recommendationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
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
    void testGetRecommendationsForUser() throws Exception {
        // Given
        String userId = "user1";
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationService.generateRecommendationsForUser(userId, 10))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user1"))
                .andExpect(jsonPath("$[0].type").value("COLLABORATIVE"))
                .andExpect(jsonPath("$[0].score").value(0.8));
        
        verify(recommendationService).generateRecommendationsForUser(userId, 10);
    }
    
    @Test
    void testGetRecommendationsForUserWithCustomLimit() throws Exception {
        // Given
        String userId = "user1";
        int limit = 5;
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationService.generateRecommendationsForUser(userId, limit))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/{userId}", userId)
                .param("limit", String.valueOf(limit)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
        
        verify(recommendationService).generateRecommendationsForUser(userId, limit);
    }
    
    @Test
    void testGenerateAndSaveRecommendations() throws Exception {
        // Given
        String userId = "user1";
        List<Recommendation> recommendations = List.of(testRecommendation);
        when(recommendationService.generateRecommendationsForUser(userId, 10))
            .thenReturn(recommendations);
        when(recommendationService.saveRecommendations(recommendations))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(post("/api/recommendations/{userId}/generate", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user1"));
        
        verify(recommendationService).generateRecommendationsForUser(userId, 10);
        verify(recommendationService).saveRecommendations(recommendations);
    }
    
    @Test
    void testGetSavedRecommendationsForUser() throws Exception {
        // Given
        String userId = "user1";
        Page<Recommendation> recommendationPage = new PageImpl<>(List.of(testRecommendation));
        when(recommendationService.getRecommendationsForUser(eq(userId), any(PageRequest.class)))
            .thenReturn(recommendationPage);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/{userId}/saved", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].userId").value("user1"));
        
        verify(recommendationService).getRecommendationsForUser(eq(userId), any(PageRequest.class));
    }
    
    @Test
    void testGetSavedRecommendationsForUserWithPagination() throws Exception {
        // Given
        String userId = "user1";
        int page = 1;
        int size = 5;
        Page<Recommendation> recommendationPage = new PageImpl<>(List.of(testRecommendation));
        when(recommendationService.getRecommendationsForUser(eq(userId), any(PageRequest.class)))
            .thenReturn(recommendationPage);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/{userId}/saved", userId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
        
        verify(recommendationService).getRecommendationsForUser(eq(userId), eq(PageRequest.of(page, size)));
    }
    
    @Test
    void testGetPopularRecommendations() throws Exception {
        // Given
        Recommendation popularRecommendation = new Recommendation();
        popularRecommendation.setId(2L);
        popularRecommendation.setBook(testBook);
        popularRecommendation.setType(RecommendationType.POPULAR);
        popularRecommendation.setScore(0.9);
        popularRecommendation.setReason("Popular book");
        
        List<Recommendation> recommendations = List.of(popularRecommendation);
        when(recommendationService.generatePopularRecommendations(10))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/popular"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].type").value("POPULAR"))
                .andExpect(jsonPath("$[0].score").value(0.9));
        
        verify(recommendationService).generatePopularRecommendations(10);
    }
    
    @Test
    void testGetTrendingRecommendations() throws Exception {
        // Given
        Recommendation trendingRecommendation = new Recommendation();
        trendingRecommendation.setId(3L);
        trendingRecommendation.setBook(testBook);
        trendingRecommendation.setType(RecommendationType.TRENDING);
        trendingRecommendation.setScore(0.7);
        trendingRecommendation.setReason("Trending book");
        
        List<Recommendation> recommendations = List.of(trendingRecommendation);
        when(recommendationService.generateTrendingRecommendations(10))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/trending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].type").value("TRENDING"))
                .andExpect(jsonPath("$[0].score").value(0.7));
        
        verify(recommendationService).generateTrendingRecommendations(10);
    }
    
    @Test
    void testGetCollaborativeRecommendations() throws Exception {
        // Given
        String userId = "user1";
        Recommendation collaborativeRecommendation = new Recommendation();
        collaborativeRecommendation.setId(4L);
        collaborativeRecommendation.setUserId(userId);
        collaborativeRecommendation.setBook(testBook);
        collaborativeRecommendation.setType(RecommendationType.COLLABORATIVE);
        collaborativeRecommendation.setScore(0.85);
        
        List<Recommendation> recommendations = List.of(collaborativeRecommendation);
        when(recommendationService.generateCollaborativeFilteringRecommendations(userId, 10))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/collaborative/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].type").value("COLLABORATIVE"))
                .andExpect(jsonPath("$[0].score").value(0.85));
        
        verify(recommendationService).generateCollaborativeFilteringRecommendations(userId, 10);
    }
    
    @Test
    void testGetContentBasedRecommendations() throws Exception {
        // Given
        String userId = "user1";
        Recommendation contentBasedRecommendation = new Recommendation();
        contentBasedRecommendation.setId(5L);
        contentBasedRecommendation.setUserId(userId);
        contentBasedRecommendation.setBook(testBook);
        contentBasedRecommendation.setType(RecommendationType.CONTENT_BASED);
        contentBasedRecommendation.setScore(0.75);
        
        List<Recommendation> recommendations = List.of(contentBasedRecommendation);
        when(recommendationService.generateContentBasedRecommendations(userId, 10))
            .thenReturn(recommendations);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/content-based/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].type").value("CONTENT_BASED"))
                .andExpect(jsonPath("$[0].score").value(0.75));
        
        verify(recommendationService).generateContentBasedRecommendations(userId, 10);
    }
    
    @Test
    void testGetRecommendationsByType() throws Exception {
        // Given
        RecommendationType type = RecommendationType.POPULAR;
        Page<Recommendation> recommendationPage = new PageImpl<>(List.of(testRecommendation));
        when(recommendationService.getRecommendationsByType(eq(type), any(PageRequest.class)))
            .thenReturn(recommendationPage);
        
        // When & Then
        mockMvc.perform(get("/api/recommendations/type/{type}", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
        
        verify(recommendationService).getRecommendationsByType(eq(type), any(PageRequest.class));
    }
    
    @Test
    void testCleanupOldRecommendations() throws Exception {
        // Given
        String userId = "user1";
        int daysToKeep = 30;
        
        // When & Then
        mockMvc.perform(delete("/api/recommendations/{userId}/cleanup", userId)
                .param("daysToKeep", String.valueOf(daysToKeep)))
                .andExpect(status().isOk());
        
        verify(recommendationService).cleanupOldRecommendations(userId, daysToKeep);
    }
    
    @Test
    void testGetRecommendationsForUserWithInvalidLimit() throws Exception {
        // Given
        String userId = "user1";
        
        // When & Then - Test limit too small
        mockMvc.perform(get("/api/recommendations/{userId}", userId)
                .param("limit", "0"))
                .andExpect(status().isBadRequest());
        
        // When & Then - Test limit too large
        mockMvc.perform(get("/api/recommendations/{userId}", userId)
                .param("limit", "100"))
                .andExpect(status().isBadRequest());
        
        verify(recommendationService, never()).generateRecommendationsForUser(anyString(), anyInt());
    }
    
    @Test
    void testGetSavedRecommendationsWithInvalidPagination() throws Exception {
        // Given
        String userId = "user1";
        
        // When & Then - Test negative page
        mockMvc.perform(get("/api/recommendations/{userId}/saved", userId)
                .param("page", "-1"))
                .andExpect(status().isBadRequest());
        
        // When & Then - Test size too large
        mockMvc.perform(get("/api/recommendations/{userId}/saved", userId)
                .param("size", "200"))
                .andExpect(status().isBadRequest());
        
        verify(recommendationService, never()).getRecommendationsForUser(anyString(), any());
    }
    
    @Test
    void testCleanupOldRecommendationsWithInvalidDays() throws Exception {
        // Given
        String userId = "user1";
        
        // When & Then - Test days too small
        mockMvc.perform(delete("/api/recommendations/{userId}/cleanup", userId)
                .param("daysToKeep", "0"))
                .andExpect(status().isBadRequest());
        
        // When & Then - Test days too large
        mockMvc.perform(delete("/api/recommendations/{userId}/cleanup", userId)
                .param("daysToKeep", "400"))
                .andExpect(status().isBadRequest());
        
        verify(recommendationService, never()).cleanupOldRecommendations(anyString(), anyInt());
    }
}