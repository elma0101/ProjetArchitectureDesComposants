package com.bookstore.recommendation.client;

import com.bookstore.recommendation.dto.BookResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation for Book Catalog Service with graceful degradation
 * Returns empty results instead of failing when the service is unavailable
 */
@Slf4j
@Component
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
        log.warn("Fallback: Unable to fetch book with id: {}. Returning null for graceful degradation.", id);
        fallbackCounter.increment();
        
        // Graceful degradation: return null to allow recommendation service to continue
        // with cached or alternative data
        return null;
    }
    
    @Override
    public List<BookResponse> getAllBooks() {
        log.warn("Fallback: Unable to fetch books. Returning empty list for graceful degradation.");
        fallbackCounter.increment();
        
        // Graceful degradation: return empty list to allow recommendation service to continue
        // with cached or alternative data
        return Collections.emptyList();
    }
}
