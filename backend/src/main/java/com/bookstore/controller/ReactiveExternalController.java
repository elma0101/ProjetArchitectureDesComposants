package com.bookstore.controller;

import com.bookstore.client.ReactiveExternalService;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Controller for reactive external service operations
 */
@RestController
@RequestMapping("/api/reactive")
@Tag(name = "Reactive External Services", description = "Reactive operations for external service integration")
public class ReactiveExternalController {

    private final ReactiveExternalService reactiveExternalService;

    public ReactiveExternalController(ReactiveExternalService reactiveExternalService) {
        this.reactiveExternalService = reactiveExternalService;
    }

    @GetMapping("/books/{id}/complete")
    @Operation(summary = "Get complete book information with authors")
    public Mono<Map<String, Object>> getCompleteBookInfo(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        return reactiveExternalService.getCompleteBookInfo(id);
    }

    @GetMapping("/authors/{id}/complete")
    @Operation(summary = "Get author with their books")
    public Mono<Map<String, Object>> getAuthorWithBooks(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        return reactiveExternalService.getAuthorWithBooks(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search across books and authors simultaneously")
    public Mono<Map<String, Object>> searchBooksAndAuthors(
            @Parameter(description = "Search query") @RequestParam String query) {
        return reactiveExternalService.searchBooksAndAuthors(query);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular books and authors")
    public Mono<Map<String, Object>> getPopularContent(
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "10") int limit) {
        return reactiveExternalService.getPopularContent(limit);
    }

    @PostMapping("/books/batch")
    @Operation(summary = "Batch process books with author information")
    public Flux<Map<String, Object>> processBooksWithAuthors(
            @RequestBody List<Long> bookIds) {
        return reactiveExternalService.processBooksWithAuthors(bookIds);
    }

    @GetMapping(value = "/books/genre/{genre}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream books by genre with author information")
    public Flux<Map<String, Object>> streamBooksByGenreWithAuthors(
            @Parameter(description = "Book genre") @PathVariable String genre) {
        return reactiveExternalService.streamBooksByGenreWithAuthors(genre);
    }

    @GetMapping("/external")
    @Operation(summary = "Call external book service")
    public Mono<Map<String, Object>> callExternalBookService(
            @Parameter(description = "External service URL") @RequestParam String url,
            @Parameter(description = "Query parameters") @RequestParam Map<String, String> params) {
        return reactiveExternalService.callExternalBookService(url, params);
    }

    @GetMapping("/library/stats")
    @Operation(summary = "Get aggregated library statistics")
    public Mono<Map<String, Object>> getLibraryStats() {
        return reactiveExternalService.aggregateLibraryData();
    }

    @GetMapping("/health")
    @Operation(summary = "Check health of external services")
    public Mono<Map<String, Object>> checkHealth() {
        return reactiveExternalService.checkExternalServicesHealth();
    }
}