package com.bookstore.client;

import com.bookstore.entity.Author;
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
 * Reactive service for author operations using WebClient
 */
@Service
public class ReactiveAuthorService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveAuthorService.class);
    private final WebClient webClient;

    public ReactiveAuthorService(@Qualifier("authorServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Get all authors with reactive pagination
     */
    public Mono<PagedModel<Author>> getAllAuthors(int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/authors")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Author>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Successfully retrieved {} authors", 
                        result != null ? result.getContent().size() : 0))
                .doOnError(error -> logger.error("Error retrieving authors: {}", error.getMessage()));
    }

    /**
     * Get author by ID reactively
     */
    public Mono<Author> getAuthorById(Long id) {
        return webClient.get()
                .uri("/api/authors/{id}", id)
                .retrieve()
                .bodyToMono(Author.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        logger.warn("Author not found with id: {}", id);
                        return Mono.empty();
                    }
                    return handleError(error);
                })
                .doOnSuccess(author -> logger.debug("Successfully retrieved author: {} {}", 
                        author != null ? author.getFirstName() : "null",
                        author != null ? author.getLastName() : "null"))
                .doOnError(error -> logger.error("Error retrieving author {}: {}", id, error.getMessage()));
    }

    /**
     * Search authors by name reactively
     */
    public Mono<PagedModel<Author>> findAuthorsByName(String name) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/authors/search/findByName")
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Author>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} authors with name containing: {}", 
                        result != null ? result.getContent().size() : 0, name))
                .doOnError(error -> logger.error("Error searching authors by name '{}': {}", name, error.getMessage()));
    }

    /**
     * Search authors by first name reactively
     */
    public Mono<PagedModel<Author>> findAuthorsByFirstName(String firstName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/authors/search/findByFirstName")
                        .queryParam("firstName", firstName)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Author>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} authors with first name: {}", 
                        result != null ? result.getContent().size() : 0, firstName))
                .doOnError(error -> logger.error("Error searching authors by first name '{}': {}", firstName, error.getMessage()));
    }

    /**
     * Search authors by last name reactively
     */
    public Mono<PagedModel<Author>> findAuthorsByLastName(String lastName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/authors/search/findByLastName")
                        .queryParam("lastName", lastName)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Author>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(result -> logger.debug("Found {} authors with last name: {}", 
                        result != null ? result.getContent().size() : 0, lastName))
                .doOnError(error -> logger.error("Error searching authors by last name '{}': {}", lastName, error.getMessage()));
    }

    /**
     * Get books by author with reactive streams
     */
    public Flux<Book> getBooksByAuthor(Long authorId) {
        return webClient.get()
                .uri("/api/authors/{id}/books", authorId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Book>>() {})
                .flatMapMany(pagedModel -> Flux.fromIterable(pagedModel.getContent()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    logger.error("Error retrieving books by author {}: {}", authorId, error.getMessage());
                    return Flux.empty();
                })
                .doOnNext(book -> logger.debug("Processing book by author {}: {}", authorId, book.getTitle()))
                .doOnComplete(() -> logger.debug("Completed processing books for author: {}", authorId));
    }

    /**
     * Get authors by nationality with reactive streams
     */
    public Flux<Author> getAuthorsByNationality(String nationality) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/authors/search/findByNationality")
                        .queryParam("nationality", nationality)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PagedModel<Author>>() {})
                .flatMapMany(pagedModel -> Flux.fromIterable(pagedModel.getContent()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    logger.error("Error retrieving authors by nationality '{}': {}", nationality, error.getMessage());
                    return Flux.empty();
                })
                .doOnNext(author -> logger.debug("Processing author: {} {}", author.getFirstName(), author.getLastName()))
                .doOnComplete(() -> logger.debug("Completed processing authors for nationality: {}", nationality));
    }

    /**
     * Create a new author reactively
     */
    public Mono<Author> createAuthor(Author author) {
        return webClient.post()
                .uri("/api/authors")
                .bodyValue(author)
                .retrieve()
                .bodyToMono(Author.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(createdAuthor -> logger.info("Successfully created author: {} {}", 
                        createdAuthor != null ? createdAuthor.getFirstName() : "null",
                        createdAuthor != null ? createdAuthor.getLastName() : "null"))
                .doOnError(error -> logger.error("Error creating author '{} {}': {}", 
                        author.getFirstName(), author.getLastName(), error.getMessage()));
    }

    /**
     * Update an author reactively
     */
    public Mono<Author> updateAuthor(Long id, Author author) {
        return webClient.put()
                .uri("/api/authors/{id}", id)
                .bodyValue(author)
                .retrieve()
                .bodyToMono(Author.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(this::handleError)
                .doOnSuccess(updatedAuthor -> logger.info("Successfully updated author: {} {}", 
                        updatedAuthor != null ? updatedAuthor.getFirstName() : "null",
                        updatedAuthor != null ? updatedAuthor.getLastName() : "null"))
                .doOnError(error -> logger.error("Error updating author {}: {}", id, error.getMessage()));
    }

    /**
     * Delete an author reactively
     */
    public Mono<Void> deleteAuthor(Long id) {
        return webClient.delete()
                .uri("/api/authors/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException.NotFound) {
                        logger.warn("Author not found for deletion with id: {}", id);
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .doOnSuccess(result -> logger.info("Successfully deleted author with id: {}", id))
                .doOnError(error -> logger.error("Error deleting author {}: {}", id, error.getMessage()));
    }

    /**
     * Batch process authors with reactive streams
     */
    public Flux<Author> processAuthorsInBatches(Flux<Author> authors, int batchSize) {
        return authors
                .buffer(batchSize)
                .flatMap(batch -> {
                    logger.debug("Processing batch of {} authors", batch.size());
                    return Flux.fromIterable(batch)
                            .flatMap(this::enrichAuthorData)
                            .collectList()
                            .flatMapMany(Flux::fromIterable);
                })
                .doOnNext(author -> logger.debug("Processed author: {} {}", author.getFirstName(), author.getLastName()))
                .doOnComplete(() -> logger.info("Completed batch processing of authors"));
    }

    /**
     * Enrich author data with additional information
     */
    private Mono<Author> enrichAuthorData(Author author) {
        return getBooksByAuthor(author.getId())
                .collectList()
                .map(books -> {
                    // Enrich author with book count or other metadata
                    logger.debug("Author {} {} has {} books", 
                            author.getFirstName(), author.getLastName(), books.size());
                    return author;
                })
                .onErrorReturn(author); // Return original author if enrichment fails
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
        logger.error("Unhandled error in ReactiveAuthorService: {}", error.getMessage(), error);
        return Mono.empty();
    }
}