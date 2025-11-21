package com.bookstore.performance;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for database operations and indexing
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
public class DatabasePerformanceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    @Transactional
    public void testBookSearchPerformance() {
        // Test various search operations to ensure they use indexes efficiently
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test title search (should use idx_book_title)
        long startTime = System.currentTimeMillis();
        var titleResults = bookRepository.findByTitleContainingIgnoreCase("test", pageable);
        long titleSearchTime = System.currentTimeMillis() - startTime;
        
        // Test genre search (should use idx_book_genre)
        startTime = System.currentTimeMillis();
        var genreResults = bookRepository.findByGenreContainingIgnoreCase("fiction", pageable);
        long genreSearchTime = System.currentTimeMillis() - startTime;
        
        // Test publication year search (should use idx_book_publication_year)
        startTime = System.currentTimeMillis();
        var yearResults = bookRepository.findByPublicationYear(2020, pageable);
        long yearSearchTime = System.currentTimeMillis() - startTime;
        
        // Test available books search (should use idx_book_available_copies)
        startTime = System.currentTimeMillis();
        var availableResults = bookRepository.findAvailableBooks(pageable);
        long availableSearchTime = System.currentTimeMillis() - startTime;
        
        // All searches should complete reasonably quickly
        assertTrue(titleSearchTime < 1000, "Title search took too long: " + titleSearchTime + "ms");
        assertTrue(genreSearchTime < 1000, "Genre search took too long: " + genreSearchTime + "ms");
        assertTrue(yearSearchTime < 1000, "Year search took too long: " + yearSearchTime + "ms");
        assertTrue(availableSearchTime < 1000, "Available search took too long: " + availableSearchTime + "ms");
    }

    @Test
    @Transactional
    public void testAuthorSearchPerformance() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test full name search (should use idx_author_full_name)
        long startTime = System.currentTimeMillis();
        var nameResults = authorRepository.findByFullNameContainingIgnoreCase("test", pageable);
        long nameSearchTime = System.currentTimeMillis() - startTime;
        
        // Test nationality search (should use idx_author_nationality)
        startTime = System.currentTimeMillis();
        var nationalityResults = authorRepository.findByNationalityContainingIgnoreCase("american", pageable);
        long nationalitySearchTime = System.currentTimeMillis() - startTime;
        
        // All searches should complete reasonably quickly
        assertTrue(nameSearchTime < 1000, "Name search took too long: " + nameSearchTime + "ms");
        assertTrue(nationalitySearchTime < 1000, "Nationality search took too long: " + nationalitySearchTime + "ms");
    }

    @Test
    @Transactional
    public void testLoanSearchPerformance() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test borrower search (should use idx_loan_borrower_email)
        long startTime = System.currentTimeMillis();
        var borrowerResults = loanRepository.findByBorrowerEmailIgnoreCase("test@example.com", pageable);
        long borrowerSearchTime = System.currentTimeMillis() - startTime;
        
        // Test date range search (should use idx_loan_date)
        startTime = System.currentTimeMillis();
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        var dateResults = loanRepository.findByLoanDateBetween(startDate, endDate, pageable);
        long dateSearchTime = System.currentTimeMillis() - startTime;
        
        // All searches should complete reasonably quickly
        assertTrue(borrowerSearchTime < 1000, "Borrower search took too long: " + borrowerSearchTime + "ms");
        assertTrue(dateSearchTime < 1000, "Date search took too long: " + dateSearchTime + "ms");
    }

    @Test
    @Transactional
    public void testPaginationPerformance() {
        // Test pagination performance with different page sizes
        
        // Small page
        long startTime = System.currentTimeMillis();
        var smallPage = bookRepository.findAll(PageRequest.of(0, 10));
        long smallPageTime = System.currentTimeMillis() - startTime;
        
        // Medium page
        startTime = System.currentTimeMillis();
        var mediumPage = bookRepository.findAll(PageRequest.of(0, 50));
        long mediumPageTime = System.currentTimeMillis() - startTime;
        
        // Large page
        startTime = System.currentTimeMillis();
        var largePage = bookRepository.findAll(PageRequest.of(0, 100));
        long largePageTime = System.currentTimeMillis() - startTime;
        
        // Pagination should scale reasonably
        assertTrue(smallPageTime < 500, "Small page took too long: " + smallPageTime + "ms");
        assertTrue(mediumPageTime < 1000, "Medium page took too long: " + mediumPageTime + "ms");
        assertTrue(largePageTime < 2000, "Large page took too long: " + largePageTime + "ms");
        
        // Verify pagination works correctly
        assertTrue(smallPage.hasContent());
        assertTrue(mediumPage.hasContent());
        assertTrue(largePage.hasContent());
    }

    @Test
    @Transactional
    public void testComplexQueryPerformance() {
        // Test complex queries that join multiple tables
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Test book search by author name (joins books and authors)
        long startTime = System.currentTimeMillis();
        var authorBookResults = bookRepository.findByAuthorNameContainingIgnoreCase("test", pageable);
        long authorBookSearchTime = System.currentTimeMillis() - startTime;
        
        // Test multi-criteria search
        startTime = System.currentTimeMillis();
        var multiResults = bookRepository.searchBooks("test", "fiction", "author", 2020, false, pageable);
        long multiSearchTime = System.currentTimeMillis() - startTime;
        
        // Complex queries should still be reasonably fast
        assertTrue(authorBookSearchTime < 2000, "Author-book search took too long: " + authorBookSearchTime + "ms");
        assertTrue(multiSearchTime < 2000, "Multi-criteria search took too long: " + multiSearchTime + "ms");
    }

    @Test
    @Transactional
    public void testBulkOperationPerformance() {
        // Test bulk operations performance
        
        // Create test data
        List<Book> testBooks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Book book = new Book();
            book.setTitle("Performance Test Book " + i);
            book.setIsbn("978-0000000" + String.format("%03d", i));
            book.setGenre("Test");
            book.setPublicationYear(2020);
            book.setAvailableCopies(5);
            book.setTotalCopies(5);
            testBooks.add(book);
        }
        
        // Test bulk save
        long startTime = System.currentTimeMillis();
        bookRepository.saveAll(testBooks);
        long bulkSaveTime = System.currentTimeMillis() - startTime;
        
        // Bulk operations should be efficient
        assertTrue(bulkSaveTime < 5000, "Bulk save took too long: " + bulkSaveTime + "ms");
        
        // Verify data was saved
        long count = bookRepository.count();
        assertTrue(count >= 100, "Not all books were saved");
    }

    @Test
    public void testConnectionPoolPerformance() {
        // Test that connection pooling is working efficiently
        // This test simulates concurrent database access
        
        List<Thread> threads = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                bookRepository.count(); // Simple query
                long executionTime = System.currentTimeMillis() - startTime;
                synchronized (executionTimes) {
                    executionTimes.add(executionTime);
                }
            });
            threads.add(thread);
        }
        
        // Start all threads
        long overallStart = System.currentTimeMillis();
        threads.forEach(Thread::start);
        
        // Wait for all threads to complete
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long overallTime = System.currentTimeMillis() - overallStart;
        
        // All queries should complete reasonably quickly
        assertTrue(overallTime < 5000, "Concurrent queries took too long: " + overallTime + "ms");
        assertEquals(10, executionTimes.size(), "Not all queries completed");
        
        // Individual queries should be fast
        executionTimes.forEach(time -> 
            assertTrue(time < 1000, "Individual query took too long: " + time + "ms"));
    }
}