package com.bookstore.client;

import com.bookstore.entity.Author;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service wrapper for AuthorClient with circuit breaker pattern
 */
@Service
public class ExternalAuthorService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthorService.class);
    private static final String AUTHOR_SERVICE = "authorService";
    
    @Autowired
    private AuthorClient authorClient;
    
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "fallbackGetAllAuthors")
    @Retry(name = AUTHOR_SERVICE)
    @TimeLimiter(name = AUTHOR_SERVICE)
    public CompletableFuture<PagedModel<Author>> getAllAuthors(int page, int size) {
        return CompletableFuture.supplyAsync(() -> authorClient.getAllAuthors(page, size));
    }
    
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "fallbackGetAuthorById")
    @Retry(name = AUTHOR_SERVICE)
    @TimeLimiter(name = AUTHOR_SERVICE)
    public CompletableFuture<Author> getAuthorById(Long id) {
        return CompletableFuture.supplyAsync(() -> authorClient.getAuthorById(id));
    }
    
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "fallbackFindAuthorsByName")
    @Retry(name = AUTHOR_SERVICE)
    @TimeLimiter(name = AUTHOR_SERVICE)
    public CompletableFuture<PagedModel<Author>> findAuthorsByName(String name) {
        return CompletableFuture.supplyAsync(() -> authorClient.findAuthorsByName(name));
    }
    
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "fallbackFindAuthorsByFirstName")
    @Retry(name = AUTHOR_SERVICE)
    @TimeLimiter(name = AUTHOR_SERVICE)
    public CompletableFuture<PagedModel<Author>> findAuthorsByFirstName(String firstName) {
        return CompletableFuture.supplyAsync(() -> authorClient.findAuthorsByFirstName(firstName));
    }
    
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "fallbackFindAuthorsByLastName")
    @Retry(name = AUTHOR_SERVICE)
    @TimeLimiter(name = AUTHOR_SERVICE)
    public CompletableFuture<PagedModel<Author>> findAuthorsByLastName(String lastName) {
        return CompletableFuture.supplyAsync(() -> authorClient.findAuthorsByLastName(lastName));
    }
    
    // Fallback methods
    public CompletableFuture<PagedModel<Author>> fallbackGetAllAuthors(int page, int size, Exception ex) {
        logger.warn("Fallback triggered for getAllAuthors - page: {}, size: {}, error: {}", page, size, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<Author> fallbackGetAuthorById(Long id, Exception ex) {
        logger.warn("Fallback triggered for getAuthorById - id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }
    
    public CompletableFuture<PagedModel<Author>> fallbackFindAuthorsByName(String name, Exception ex) {
        logger.warn("Fallback triggered for findAuthorsByName - name: {}, error: {}", name, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<PagedModel<Author>> fallbackFindAuthorsByFirstName(String firstName, Exception ex) {
        logger.warn("Fallback triggered for findAuthorsByFirstName - firstName: {}, error: {}", firstName, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
    
    public CompletableFuture<PagedModel<Author>> fallbackFindAuthorsByLastName(String lastName, Exception ex) {
        logger.warn("Fallback triggered for findAuthorsByLastName - lastName: {}, error: {}", lastName, ex.getMessage());
        return CompletableFuture.completedFuture(PagedModel.empty());
    }
}