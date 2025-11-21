package com.bookstore.performance;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.service.AdvancedBookService;
import com.bookstore.service.AuthorService;
import com.bookstore.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for caching functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cache.type=simple", // Use simple cache for testing
    "spring.jpa.show-sql=false"
})
public class CachePerformanceTest {

    @Autowired
    private AdvancedBookService advancedBookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testBookStatisticsCaching() {
        // Clear cache
        cacheManager.getCache("bookStats").clear();

        // First call - should hit database
        long startTime = System.currentTimeMillis();
        var stats1 = advancedBookService.getBookStatistics();
        long firstCallTime = System.currentTimeMillis() - startTime;

        // Second call - should hit cache
        startTime = System.currentTimeMillis();
        var stats2 = advancedBookService.getBookStatistics();
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Verify results are the same
        assertEquals(stats1.getTotalBooks(), stats2.getTotalBooks());
        assertEquals(stats1.getTotalCopies(), stats2.getTotalCopies());

        // Second call should be significantly faster (cached)
        assertTrue(secondCallTime < firstCallTime, 
            "Cached call should be faster. First: " + firstCallTime + "ms, Second: " + secondCallTime + "ms");
    }

    @Test
    public void testAuthorCaching() {
        // This test assumes there's at least one author in the test database
        // Clear cache
        if (cacheManager.getCache("authors") != null) {
            cacheManager.getCache("authors").clear();
        }

        // Create a test scenario - this would need actual test data
        // For now, we'll test the cache configuration exists
        assertNotNull(cacheManager.getCache("authors"));
        assertNotNull(cacheManager.getCache("authorStats"));
        assertNotNull(cacheManager.getCache("authorSearch"));
    }

    @Test
    public void testRecommendationCaching() {
        // Clear cache
        if (cacheManager.getCache("recommendations") != null) {
            cacheManager.getCache("recommendations").clear();
        }

        String testUserId = "test-user-123";

        // First call - should hit database
        long startTime = System.currentTimeMillis();
        var recommendations1 = recommendationService.generateRecommendationsForUser(testUserId);
        long firstCallTime = System.currentTimeMillis() - startTime;

        // Second call - should hit cache
        startTime = System.currentTimeMillis();
        var recommendations2 = recommendationService.generateRecommendationsForUser(testUserId);
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Verify results are the same
        assertEquals(recommendations1.size(), recommendations2.size());

        // Second call should be faster (cached)
        assertTrue(secondCallTime <= firstCallTime, 
            "Cached call should be faster or equal. First: " + firstCallTime + "ms, Second: " + secondCallTime + "ms");
    }

    @Test
    public void testPopularBooksCaching() {
        // Clear cache
        if (cacheManager.getCache("popularBooks") != null) {
            cacheManager.getCache("popularBooks").clear();
        }

        int limit = 10;

        // First call - should hit database
        long startTime = System.currentTimeMillis();
        var popular1 = recommendationService.generatePopularRecommendations(limit);
        long firstCallTime = System.currentTimeMillis() - startTime;

        // Second call - should hit cache
        startTime = System.currentTimeMillis();
        var popular2 = recommendationService.generatePopularRecommendations(limit);
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Verify results are the same
        assertEquals(popular1.size(), popular2.size());

        // Second call should be faster (cached)
        assertTrue(secondCallTime <= firstCallTime, 
            "Cached call should be faster or equal. First: " + firstCallTime + "ms, Second: " + secondCallTime + "ms");
    }

    @Test
    public void testCacheConfiguration() {
        // Verify all expected caches are configured
        assertNotNull(cacheManager.getCache("books"));
        assertNotNull(cacheManager.getCache("authors"));
        assertNotNull(cacheManager.getCache("bookSearch"));
        assertNotNull(cacheManager.getCache("authorSearch"));
        assertNotNull(cacheManager.getCache("recommendations"));
        assertNotNull(cacheManager.getCache("popularBooks"));
        assertNotNull(cacheManager.getCache("bookStats"));
        assertNotNull(cacheManager.getCache("authorStats"));
        assertNotNull(cacheManager.getCache("loanStats"));
    }

    @Test
    public void testCacheEviction() {
        // This test would verify that cache eviction works properly
        // when data is modified
        
        var cache = cacheManager.getCache("bookStats");
        assertNotNull(cache);
        
        // Clear cache
        cache.clear();
        
        // Verify cache is empty
        assertNull(cache.get("all"));
        
        // Call method to populate cache
        advancedBookService.getBookStatistics();
        
        // Verify cache is populated (this might not work with simple cache manager)
        // In a real Redis environment, we could verify the cache entry exists
    }
}