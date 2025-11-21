package com.bookstore.client;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Reactive service that combines book and author operations for external API calls
 */
@Service
public class ReactiveExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveExternalService.class);
    private final ReactiveBookService bookService;
    private final ReactiveAuthorService authorService;
    private final WebClient genericWebClient;

    public ReactiveExternalService(ReactiveBookService bookService, 
                                 ReactiveAuthorService authorService,
                                 @Qualifier("genericWebClient") WebClient genericWebClient) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genericWebClient = genericWebClient;
    }

    /**
     * Get complete book information with author details
     */
    public Mono<Map<String, Object>> getCompleteBookInfo(Long bookId) {
        return bookService.getBookById(bookId)
                .flatMap(book -> {
                    if (book == null) {
                        return Mono.just(Map.<String, Object>of("error", "Book not found"));
                    }
                    
                    // Get author information for the book
                    Flux<Author> authorFlux = Flux.fromIterable(book.getAuthors());
                    
                    return authorFlux
                            .flatMap(author -> authorService.getAuthorById(author.getId()))
                            .collectList()
                            .map(authors -> Map.<String, Object>of(
                                    "book", book,
                                    "authors", authors,
                                    "authorCount", authors.size()
                            ));
                })
                .doOnSuccess(result -> logger.debug("Retrieved complete book info for book ID: {}", bookId))
                .doOnError(error -> logger.error("Error retrieving complete book info for ID {}: {}", bookId, error.getMessage()));
    }

    /**
     * Get author with their books using reactive streams
     */
    public Mono<Map<String, Object>> getAuthorWithBooks(Long authorId) {
        return authorService.getAuthorById(authorId)
                .flatMap(author -> {
                    if (author == null) {
                        return Mono.just(Map.<String, Object>of("error", "Author not found"));
                    }
                    
                    return authorService.getBooksByAuthor(authorId)
                            .collectList()
                            .map(books -> Map.<String, Object>of(
                                    "author", author,
                                    "books", books,
                                    "bookCount", books.size()
                            ));
                })
                .doOnSuccess(result -> logger.debug("Retrieved author with books for author ID: {}", authorId))
                .doOnError(error -> logger.error("Error retrieving author with books for ID {}: {}", authorId, error.getMessage()));
    }

    /**
     * Search across books and authors simultaneously
     */
    public Mono<Map<String, Object>> searchBooksAndAuthors(String query) {
        Mono<PagedModel<Book>> bookResults = bookService.findBooksByTitle(query)
                .onErrorReturn(PagedModel.empty());
        
        Mono<PagedModel<Author>> authorResults = authorService.findAuthorsByName(query)
                .onErrorReturn(PagedModel.empty());
        
        return Mono.zip(bookResults, authorResults)
                .map(tuple -> Map.<String, Object>of(
                        "books", tuple.getT1().getContent(),
                        "authors", tuple.getT2().getContent(),
                        "bookCount", tuple.getT1().getContent().size(),
                        "authorCount", tuple.getT2().getContent().size(),
                        "query", query
                ))
                .doOnSuccess(result -> logger.debug("Search completed for query: {}", query))
                .doOnError(error -> logger.error("Error during search for query '{}': {}", query, error.getMessage()));
    }

    /**
     * Get popular books and authors in parallel
     */
    public Mono<Map<String, Object>> getPopularContent(int limit) {
        Mono<List<Book>> popularBooks = bookService.getAllBooks(0, limit)
                .map(pagedModel -> pagedModel.getContent().stream().limit(limit).toList())
                .onErrorReturn(List.of());
        
        Mono<List<Author>> popularAuthors = authorService.getAllAuthors(0, limit)
                .map(pagedModel -> pagedModel.getContent().stream().limit(limit).toList())
                .onErrorReturn(List.of());
        
        return Mono.zip(popularBooks, popularAuthors)
                .map(tuple -> Map.<String, Object>of(
                        "popularBooks", tuple.getT1(),
                        "popularAuthors", tuple.getT2(),
                        "timestamp", System.currentTimeMillis()
                ))
                .doOnSuccess(result -> logger.debug("Retrieved popular content"))
                .doOnError(error -> logger.error("Error retrieving popular content: {}", error.getMessage()));
    }

    /**
     * Batch process books with author enrichment
     */
    public Flux<Map<String, Object>> processBooksWithAuthors(List<Long> bookIds) {
        return Flux.fromIterable(bookIds)
                .flatMap(this::getCompleteBookInfo, 5) // Limit concurrency to 5
                .filter(bookInfo -> !bookInfo.containsKey("error"))
                .doOnNext(bookInfo -> logger.debug("Processed book with authors"))
                .doOnComplete(() -> logger.info("Completed batch processing of {} books", bookIds.size()))
                .doOnError(error -> logger.error("Error in batch processing: {}", error.getMessage()));
    }

    /**
     * Stream books by genre with author information
     */
    public Flux<Map<String, Object>> streamBooksByGenreWithAuthors(String genre) {
        return bookService.getBooksByGenre(genre)
                .flatMap(book -> {
                    Flux<Author> authorFlux = Flux.fromIterable(book.getAuthors());
                    return authorFlux
                            .flatMap(author -> authorService.getAuthorById(author.getId()))
                            .collectList()
                            .map(authors -> Map.<String, Object>of(
                                    "book", book,
                                    "authors", authors
                            ));
                })
                .doOnNext(bookWithAuthors -> logger.debug("Streamed book with authors for genre: {}", genre))
                .doOnComplete(() -> logger.info("Completed streaming books for genre: {}", genre))
                .doOnError(error -> logger.error("Error streaming books for genre '{}': {}", genre, error.getMessage()));
    }

    /**
     * Make external API call to third-party service
     */
    public Mono<Map<String, Object>> callExternalBookService(String externalUrl, Map<String, String> params) {
        return genericWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(externalUrl);
                    params.forEach(builder::queryParam);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> !(throwable instanceof IllegalArgumentException)))
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(error -> {
                    logger.error("Error calling external service at {}: {}", externalUrl, error.getMessage());
                    return Mono.just(Map.<String, Object>of(
                            "error", "External service unavailable",
                            "message", error.getMessage()
                    ));
                })
                .doOnSuccess(result -> logger.debug("Successfully called external service: {}", externalUrl))
                .doOnError(error -> logger.error("Failed to call external service {}: {}", externalUrl, error.getMessage()));
    }

    /**
     * Aggregate data from multiple sources reactively
     */
    public Mono<Map<String, Object>> aggregateLibraryData() {
        Mono<Long> totalBooks = bookService.getAllBooks(0, 1)
                .map(pagedModel -> pagedModel.getMetadata() != null ? 
                        pagedModel.getMetadata().getTotalElements() : 0L)
                .onErrorReturn(0L);
        
        Mono<Long> totalAuthors = authorService.getAllAuthors(0, 1)
                .map(pagedModel -> pagedModel.getMetadata() != null ? 
                        pagedModel.getMetadata().getTotalElements() : 0L)
                .onErrorReturn(0L);
        
        return Mono.zip(totalBooks, totalAuthors)
                .map(tuple -> Map.<String, Object>of(
                        "totalBooks", tuple.getT1(),
                        "totalAuthors", tuple.getT2(),
                        "lastUpdated", System.currentTimeMillis(),
                        "status", "active"
                ))
                .doOnSuccess(result -> logger.info("Aggregated library data: {} books, {} authors", 
                        result.get("totalBooks"), result.get("totalAuthors")))
                .doOnError(error -> logger.error("Error aggregating library data: {}", error.getMessage()));
    }

    /**
     * Health check for external services
     */
    public Mono<Map<String, Object>> checkExternalServicesHealth() {
        Mono<Boolean> bookServiceHealth = bookService.getAllBooks(0, 1)
                .map(result -> true)
                .onErrorReturn(false);
        
        Mono<Boolean> authorServiceHealth = authorService.getAllAuthors(0, 1)
                .map(result -> true)
                .onErrorReturn(false);
        
        return Mono.zip(bookServiceHealth, authorServiceHealth)
                .map(tuple -> Map.<String, Object>of(
                        "bookService", tuple.getT1() ? "healthy" : "unhealthy",
                        "authorService", tuple.getT2() ? "healthy" : "unhealthy",
                        "overall", (tuple.getT1() && tuple.getT2()) ? "healthy" : "degraded",
                        "timestamp", System.currentTimeMillis()
                ))
                .doOnSuccess(result -> logger.debug("Health check completed: {}", result))
                .doOnError(error -> logger.error("Error during health check: {}", error.getMessage()));
    }
}