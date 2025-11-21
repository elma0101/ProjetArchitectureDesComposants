package com.bookstore.client;

import com.bookstore.entity.Book;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Service wrapper for BookClient with circuit breaker pattern
 */
@Service
public class ExternalBookService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalBookService.class);
    private static final String BOOK_SERVICE = "bookService";
    
    @Autowired
    private BookClient bookClient;
    
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "fallbackGetAllBooks")
    @Retry(name = BOOK_SERVICE)
    @TimeLimiter(name = BOOK_SERVICE)
    public CompletableFuture<PagedModel<Book>> getAllBooks(int page, int size) {
        return CompletableFuture.supplyAsync(() -> bookClient.getAllBooks(page, size));
    }
    
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "fallbackGetBookById")
    @Retry(name = BOOK_SERVICE)
    @TimeLimiter(name = BOOK_SERVICE)
    public CompletableFuture<Book> getBookById(Long id) {
        return CompletableFuture.supplyAsync(() -> bookClient.getBookById(id));
    }
    
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "fallbackFindBooksByTitle")
    @Retry(name = BOOK_SERVICE)
    @TimeLimiter(name = BOOK_SERVICE)
    public CompletableFuture<PagedModel<Book>> findBooksByTitle(String title) {
        return CompletableFuture.supplyAsync(() -> bookClient.findBooksByTitle(title));
    }
    
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "fallbackFindBooksByAuthor")
    @Retry(name = BOOK_SERVICE)
    @TimeLimiter(name = BOOK_SERVICE)
    public CompletableFuture<PagedModel<Book>> findBooksByAuthor(String author) {
        return CompletableFuture.supplyAsync(() -> bookClient.findBooksByAuthor(author));
    }
    
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "fallbackFindBooksByIsbn")
    @Retry(name = BOOK_SERVICE)
    @TimeLimiter(name = BOOK_SERVICE)
    public CompletableFuture<PagedModel<Book>> findBooksByIsbn(String isbn) {
        return CompletableFuture.supplyAsync(() -> bookClient.findBooksByIsbn(isbn));
    }
    
    // Fallback methods
    public CompletableFuture<PagedModel<Book>> fallbackGetAllBooks(int page, int size, Exception ex) {
        logger.warn("Fallback triggered for getAllBooks - page: {}, size: {}, error: {}", page, size, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<Book> fallbackGetBookById(Long id, Exception ex) {
        logger.warn("Fallback triggered for getBookById - id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }
    
    public CompletableFuture<PagedModel<Book>> fallbackFindBooksByTitle(String title, Exception ex) {
        logger.warn("Fallback triggered for findBooksByTitle - title: {}, error: {}", title, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<PagedModel<Book>> fallbackFindBooksByAuthor(String author, Exception ex) {
        logger.warn("Fallback triggered for findBooksByAuthor - author: {}, error: {}", author, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<PagedModel<Book>> fallbackFindBooksByIsbn(String isbn, Exception ex) {
        logger.warn("Fallback triggered for findBooksByIsbn - isbn: {}, error: {}", isbn, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
}