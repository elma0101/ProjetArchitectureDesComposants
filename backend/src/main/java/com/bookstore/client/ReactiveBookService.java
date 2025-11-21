package com.bookstore.client;

import com.bookstore.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Reactive service for book operations using WebClient
 */
@Service
public class ReactiveBookService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveBookService.class);
    private final WebClient webClient;

    public ReactiveBookService(@Qualifier("bookServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Get all books with reactive pagination
     */
    public Mono<PagedModel<Book>> getAllBooks(int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Successfully retrieved {} books", 
                        result != null ? result.getContent().size() : 0))
                .doOnError(error -> logger.error("Error retrieving books: {}", error.getMessage()));
    }

    /**
     * Get book by ID reactively
     */
    public Mono<Book> getBookById(Long id) {
        return webClient.get()
                .uri("/api/books/{id}", id)
                .retrieve()
                .bodyToMono(Book.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        logger.warn("Book not found with id: {}", id);
                        return Mono.empty();
                    }
                    return handleError(error);
                })
                .doOnSuccess(book -> logger.debug("Successfully retrieved book: {}", 
                        book != null ? book.getTitle() : "null"))
                .doOnError(error -> logger.error("Error retrieving book {}: {}", id, error.getMessage()));
    }

    /**
     * Search books by title reactively
     */
    public Mono<PagedModel<Book>> findBooksByTitle(String title) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books/search/findByTitle")
                        .queryParam("title", title)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} books with title containing: {}", 
                        result != null ? result.getContent().size() : 0, title))
                .doOnError(error -> logger.error("Error searching books by title '{}': {}", title, error.getMessage()));
    }

    /**
     * Search books by author reactively
     */
    public Mono<PagedModel<Book>> findBooksByAuthor(String author) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books/search/findByAuthor")
                        .queryParam("author", author)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} books by author: {}", 
                        result != null ? result.getContent().size() : 0, author))
                .doOnError(error -> logger.error("Error searching books by author '{}': {}", author, error.getMessage()));
    }

    /**
     * Search books by ISBN reactively
     */
    public Mono<PagedModel<Book>> findBooksByIsbn(String isbn) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books/search/findByIsbn")
                        .queryParam("isbn", isbn)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} books with ISBN: {}", 
                        result != null ? result.getContent().size() : 0, isbn))
                .doOnError(error -> logger.error("Error searching books by ISBN '{}': {}", isbn, error.getMessage()));
    }

    /**
     * Get books by genre with reactive streams
     */
    public Flux<Book> getBooksByGenre(String genre) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books/search/findByGenre")
                        .queryParam("genre", genre)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .flatMapMany(pagedModel -> Flux.fromIterable(pagedModel.getContent()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    logger.error("Error retrieving books by genre '{}': {}", genre, error.getMessage());
                    return Flux.empty();
                })
                .doOnNext(book -> logger.debug("Processing book: {}", book.getTitle()))
                .doOnComplete(() -> logger.debug("Completed processing books for genre: {}", genre));
    }

    /**
     * Create a new book reactively
     */
    public Mono<Book> createBook(Book book) {
        return webClient.post()
                .uri("/api/books")
                .bodyValue(book)
                .retrieve()
                .bodyToMono(Book.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(createdBook -> logger.info("Successfully created book: {}", 
                        createdBook != null ? createdBook.getTitle() : "null"))
                .doOnError(error -> logger.error("Error creating book '{}': {}", book.getTitle(), error.getMessage()));
    }

    /**
     * Update a book reactively
     */
    public Mono<Book> updateBook(Long id, Book book) {
        return webClient.put()
                .uri("/api/books/{id}", id)
                .bodyValue(book)
                .retrieve()
                .bodyToMono(Book.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(updatedBook -> logger.info("Successfully updated book: {}", 
                        updatedBook != null ? updatedBook.getTitle() : "null"))
                .doOnError(error -> logger.error("Error updating book {}: {}", id, error.getMessage()));
    }

    /**
     * Delete a book reactively
     */
    public Mono<Void> deleteBook(Long id) {
        return webClient.delete()
                .uri("/api/books/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        logger.warn("Book not found for deletion with id: {}", id);
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .doOnSuccess(result -> logger.info("Successfully deleted book with id: {}", id))
                .doOnError(error -> logger.error("Error deleting book {}: {}", id, error.getMessage()));
    }

    /**
     * Determines if an exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
            // Retry on server errors and some client errors, but not on 4xx client errors
            return status.is5xxServerError() || 
                   status == HttpStatus.REQUEST_TIMEOUT ||
                   status == HttpStatus.TOO_MANY_REQUESTS;
        }
        // Retry on network-related exceptions
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof java.util.concurrent.TimeoutException ||
               throwable instanceof reactor.netty.http.client.PrematureCloseException;
    }

    /**
     * Generic error handler that returns empty results for fallback
     */
    private <T> Mono<T> handleError(Throwable error) {
        logger.error("Unhandled error in ReactiveBookService: {}", error.getMessage(), error);
        return Mono.empty();
    }
}