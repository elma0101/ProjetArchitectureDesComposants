package com.bookstore.client;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "bookstore.external.book-service.url=http://localhost:${wiremock.server.port}",
    "bookstore.external.author-service.url=http://localhost:${wiremock.server.port}"
})
class FeignClientIntegrationTest {

    @Autowired
    private ExternalBookService externalBookService;

    @Autowired
    private ExternalAuthorService externalAuthorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldIntegrateBookServiceWithCircuitBreaker() throws Exception {
        // Given
        Book testBook = createTestBook();
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books?page=0&size=20"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        CompletableFuture<PagedModel<Book>> result = externalBookService.getAllBooks(0, 20);

        // Then
        assertThat(result).isNotNull();
        PagedModel<Book> books = result.get();
        assertThat(books.getContent()).hasSize(1);
        assertThat(books.getContent().iterator().next().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldIntegrateAuthorServiceWithCircuitBreaker() throws Exception {
        // Given
        Author testAuthor = createTestAuthor();
        PagedModel<Author> expectedResponse = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors?page=0&size=20"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        CompletableFuture<PagedModel<Author>> result = externalAuthorService.getAllAuthors(0, 20);

        // Then
        assertThat(result).isNotNull();
        PagedModel<Author> authors = result.get();
        assertThat(authors.getContent()).hasSize(1);
        assertThat(authors.getContent().iterator().next().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldHandleServiceFailureWithFallback() throws ExecutionException, InterruptedException {
        // Given - Service returns 500 error
        stubFor(get(urlEqualTo("/api/books?page=0&size=20"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal server error\"}")));

        // When - This should trigger the fallback due to circuit breaker
        CompletableFuture<PagedModel<Book>> fallbackResult = externalBookService.fallbackGetAllBooks(0, 20, new RuntimeException("Service error"));

        // Then
        assertThat(fallbackResult).isNotNull();
        PagedModel<Book> books = fallbackResult.get();
        assertThat(books.getContent()).isEmpty();
    }

    @Test
    void shouldHandleTimeoutWithFallback() throws ExecutionException, InterruptedException {
        // Given - Service takes too long to respond
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
                .withFixedDelay(5000))); // 5 second delay, longer than timeout

        // When - This should trigger the fallback due to timeout
        CompletableFuture<Book> fallbackResult = externalBookService.fallbackGetBookById(1L, new RuntimeException("Timeout"));

        // Then
        assertThat(fallbackResult).isNotNull();
        Book book = fallbackResult.get();
        assertThat(book).isNull();
    }

    @Test
    void shouldRetryOnTransientFailures() throws Exception {
        // Given - First call fails, second succeeds
        Book testBook = createTestBook();
        
        stubFor(get(urlEqualTo("/api/books/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Temporary error\"}"))
            .willSetStateTo("First Failure"));
            
        stubFor(get(urlEqualTo("/api/books/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Failure")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(testBook))));

        // When
        CompletableFuture<Book> result = externalBookService.getBookById(1L);

        // Then
        assertThat(result).isNotNull();
        Book book = result.get();
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Test Book");
    }

    private Book createTestBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setIsbn("978-0123456789");
        book.setDescription("A test book");
        book.setPublicationYear(2023);
        book.setGenre("Fiction");
        book.setAvailableCopies(5);
        book.setTotalCopies(10);

        return book;
    }

    private Author createTestAuthor() {
        Author author = new Author();
        author.setId(1L);
        author.setFirstName("John");
        author.setLastName("Doe");
        author.setBiography("A test author");
        author.setBirthDate(LocalDate.of(1980, 1, 1));
        author.setNationality("American");

        return author;
    }
}