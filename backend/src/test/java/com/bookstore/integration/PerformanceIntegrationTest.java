package com.bookstore.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StopWatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Performance integration tests to verify system behavior under load
 */
@SpringBootTest
@ActiveProfiles("integration-test")
class PerformanceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataManager testDataManager;

    private TestDataManager.TestDataSet bulkTestData;

    @BeforeEach
    void setUpBulkTestData() {
        testDataManager.cleanupAll();
        // Create a substantial amount of test data
        bulkTestData = testDataManager.createBulkTestData(50, 200, 500);
    }

    @Test
    void shouldHandleHighVolumeBookQueries() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Perform multiple concurrent book queries
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        CompletableFuture<?>[] futures = IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                                        .param("page", String.valueOf(i % 10))
                                        .param("size", "20")
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that 100 concurrent requests complete within reasonable time (e.g., 30 seconds)
        assertThat(executionTime).isLessThan(30000);
        System.out.println("100 concurrent book queries completed in: " + executionTime + "ms");
    }

    @Test
    void shouldHandleHighVolumeSearchQueries() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Perform multiple concurrent search queries
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        String[] searchTerms = {"Book", "Title", "Genre", "Author", "Fiction"};
        
        CompletableFuture<?>[] futures = IntStream.range(0, 50)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        String searchTerm = searchTerms[i % searchTerms.length];
                        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByTitleContainingIgnoreCase")
                                        .param("title", searchTerm)
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that 50 concurrent search requests complete within reasonable time
        assertThat(executionTime).isLessThan(20000);
        System.out.println("50 concurrent search queries completed in: " + executionTime + "ms");
    }

    @Test
    void shouldHandleLargePaginationRequests() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Test pagination performance with large page sizes
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .param("page", "0")
                        .param("size", "100")
                        .param("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that large pagination request completes within reasonable time
        assertThat(executionTime).isLessThan(5000);
        System.out.println("Large pagination request (100 items) completed in: " + executionTime + "ms");
    }

    @Test
    void shouldHandleConcurrentLoanOperations() throws Exception {
        // Create books with sufficient copies for concurrent loans
        testDataManager.cleanupAll();
        var book1 = testDataManager.createTestBook("Concurrent Book 1", "978-1111111111", 
                                                  "Book for concurrent testing", 2023, "Fiction", 50, 50);
        var book2 = testDataManager.createTestBook("Concurrent Book 2", "978-2222222222", 
                                                  "Another book for concurrent testing", 2023, "Fiction", 50, 50);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        // Create 40 concurrent loan requests (20 for each book)
        CompletableFuture<?>[] futures = IntStream.range(0, 40)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        Long bookId = (i % 2 == 0) ? book1.getId() : book2.getId();
                        String loanJson = String.format("""
                                {
                                    "borrowerName": "Concurrent Borrower %d",
                                    "borrowerEmail": "borrower%d@example.com",
                                    "borrowerId": "CONCURRENT%03d",
                                    "loanDate": "%s",
                                    "dueDate": "%s",
                                    "status": "ACTIVE",
                                    "book": "/api/books/%d"
                                }
                                """, 
                                i, i, i,
                                java.time.LocalDate.now().toString(),
                                java.time.LocalDate.now().plusDays(14).toString(),
                                bookId);

                        mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loanJson))
                                .andExpect(status().isCreated());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that 40 concurrent loan operations complete within reasonable time
        assertThat(executionTime).isLessThan(15000);
        System.out.println("40 concurrent loan operations completed in: " + executionTime + "ms");

        // Verify that book availability was correctly updated
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", book1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(30)); // 50 - 20 loans

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", book2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(30)); // 50 - 20 loans
    }

    @Test
    void shouldHandleComplexRelationshipQueries() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Test performance of complex relationship queries
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        CompletableFuture<?>[] futures = bulkTestData.getAuthors().stream()
                .limit(20) // Test with first 20 authors
                .map(author -> CompletableFuture.runAsync(() -> {
                    try {
                        // Query author's books
                        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}/books", author.getId())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that complex relationship queries complete within reasonable time
        assertThat(executionTime).isLessThan(10000);
        System.out.println("20 complex relationship queries completed in: " + executionTime + "ms");
    }

    @Test
    void shouldMaintainResponseTimeUnderLoad() throws Exception {
        // Warm up the system
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // Measure response times under increasing load
        int[] concurrencyLevels = {1, 5, 10, 20};
        
        for (int concurrency : concurrencyLevels) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            ExecutorService executor = Executors.newFixedThreadPool(concurrency);
            
            CompletableFuture<?>[] futures = IntStream.range(0, concurrency * 5) // 5 requests per thread
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                                            .param("page", String.valueOf(i % 5))
                                            .param("size", "20")
                                            .contentType(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            executor.shutdown();

            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            double avgResponseTime = (double) executionTime / (concurrency * 5);
            
            System.out.println(String.format("Concurrency: %d, Total time: %dms, Avg response time: %.2fms", 
                                            concurrency, executionTime, avgResponseTime));
            
            // Assert that average response time doesn't degrade significantly
            assertThat(avgResponseTime).isLessThan(1000); // Less than 1 second average
        }
    }

    @Test
    void shouldHandleMemoryIntensiveOperations() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Perform operations that might consume significant memory
        for (int i = 0; i < 10; i++) {
            // Large page size requests
            mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                            .param("page", "0")
                            .param("size", "100")
                            .param("sort", "title,asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(MockMvcRequestBuilders.get("/api/loans")
                            .param("page", "0")
                            .param("size", "100")
                            .param("sort", "loanDate,desc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        
        // Assert that memory-intensive operations complete within reasonable time
        assertThat(executionTime).isLessThan(20000);
        System.out.println("Memory-intensive operations completed in: " + executionTime + "ms");
    }
}