package com.bookstore.loanmanagement.client;

import com.bookstore.loanmanagement.dto.BookResponse;
import com.bookstore.loanmanagement.exception.ServiceUnavailableException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for Book Catalog Service with graceful degradation
 * Provides monitoring and detailed error handling when the service is unavailable
 */
@Component
@Slf4j
public class BookCatalogClientFallback implements BookCatalogClient {

    private final Counter fallbackCounter;

    @Autowired
    public BookCatalogClientFallback(MeterRegistry meterRegistry) {
        this.fallbackCounter = Counter.builder("book_catalog_fallback")
            .description("Number of times Book Catalog Service fallback was triggered")
            .tag("service", "book-catalog")
            .register(meterRegistry);
    }

    @Override
    public BookResponse getBookById(Long id) {
        log.error("Book Catalog Service is unavailable. Fallback triggered for getBookById: {}", id);
        fallbackCounter.increment();
        
        // Graceful degradation: throw exception with detailed message
        throw new ServiceUnavailableException(
            "Book Catalog Service is currently unavailable. Please try again later. Book ID: " + id
        );
    }

    @Override
    public BookResponse checkAvailability(Long id) {
        log.error("Book Catalog Service is unavailable. Fallback triggered for checkAvailability: {}", id);
        fallbackCounter.increment();
        
        // Graceful degradation: throw exception with detailed message
        throw new ServiceUnavailableException(
            "Book Catalog Service is currently unavailable. Cannot check availability for book ID: " + id
        );
    }

    @Override
    public void borrowBook(Long id) {
        log.error("Book Catalog Service is unavailable. Fallback triggered for borrowBook: {}", id);
        fallbackCounter.increment();
        
        // Critical operation - must fail fast
        throw new ServiceUnavailableException(
            "Book Catalog Service is currently unavailable. Cannot process borrow request for book ID: " + id
        );
    }

    @Override
    public void returnBook(Long id) {
        log.error("Book Catalog Service is unavailable. Fallback triggered for returnBook: {}", id);
        fallbackCounter.increment();
        
        // Critical operation - must fail fast
        throw new ServiceUnavailableException(
            "Book Catalog Service is currently unavailable. Cannot process return request for book ID: " + id
        );
    }
}
