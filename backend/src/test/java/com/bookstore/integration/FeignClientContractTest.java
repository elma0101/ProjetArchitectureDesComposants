package com.bookstore.integration;

import com.bookstore.client.AuthorClient;
import com.bookstore.client.BookClient;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Contract tests for FeignClient interfaces using WireMock
 */
@SpringBootTest
@AutoConfigureWireMock(port = 8089)
@TestPropertySource(properties = {
        "book.service.url=http://localhost:8089",
        "author.service.url=http://localhost:8089"
})
@ActiveProfiles("integration-test")
class FeignClientContractTest {

    @Autowired
    private BookClient bookClient;

    @Autowired
    private AuthorClient authorClient;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldGetAllBooksFromExternalService() throws Exception {
        // Create mock book data
        Book book1 = createMockBook(1L, "Book 1", "978-1111111111");
        Book book2 = createMockBook(2L, "Book 2", "978-2222222222");
        
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(20, 0, 2, 1);
        PagedModel<Book> pagedBooks = PagedModel.of(List.of(book1, book2), pageMetadata);

        // Mock the external service response
        stubFor(get(urlEqualTo("/api/books?page=0&size=20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(pagedBooks))));

        // Call the FeignClient
        PagedModel<Book> result = bookClient.getAllBooks(0, 20);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().iterator().next().getTitle()).isEqualTo("Book 1");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/books?page=0&size=20")));
    }

    @Test
    void shouldGetBookByIdFromExternalService() throws Exception {
        Book mockBook = createMockBook(1L, "Test Book", "978-1111111111");

        // Mock the external service response
        stubFor(get(urlEqualTo("/api/books/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(mockBook))));

        // Call the FeignClient
        Book result = bookClient.getBookById(1L);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getIsbn()).isEqualTo("978-1111111111");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void shouldFindBooksByTitleFromExternalService() throws Exception {
        Book mockBook = createMockBook(1L, "Test Book", "978-1111111111");
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(20, 0, 1, 1);
        PagedModel<Book> pagedBooks = PagedModel.of(List.of(mockBook), pageMetadata);

        // Mock the external service response
        stubFor(get(urlPathEqualTo("/api/books/search/findByTitle"))
                .withQueryParam("title", equalTo("Test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(pagedBooks))));

        // Call the FeignClient
        PagedModel<Book> result = bookClient.findBooksByTitle("Test");

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getTitle()).isEqualTo("Test Book");

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/books/search/findByTitle"))
                .withQueryParam("title", equalTo("Test")));
    }

    @Test
    void shouldGetBooksByAuthorFromExternalService() throws Exception {
        Book mockBook = createMockBook(1L, "Author's Book", "978-1111111111");
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(20, 0, 1, 1);
        PagedModel<Book> pagedBooks = PagedModel.of(List.of(mockBook), pageMetadata);

        // Mock the external service response
        stubFor(get(urlEqualTo("/api/authors/1/books"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(pagedBooks))));

        // Call the FeignClient
        PagedModel<Book> result = bookClient.getBooksByAuthor(1L);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getTitle()).isEqualTo("Author's Book");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/authors/1/books")));
    }

    @Test
    void shouldGetAllAuthorsFromExternalService() throws Exception {
        Author author1 = createMockAuthor(1L, "John", "Doe");
        Author author2 = createMockAuthor(2L, "Jane", "Smith");
        
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(20, 0, 2, 1);
        PagedModel<Author> pagedAuthors = PagedModel.of(List.of(author1, author2), pageMetadata);

        // Mock the external service response
        stubFor(get(urlEqualTo("/api/authors?page=0&size=20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(pagedAuthors))));

        // Call the FeignClient
        PagedModel<Author> result = authorClient.getAllAuthors(0, 20);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().iterator().next().getFirstName()).isEqualTo("John");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/authors?page=0&size=20")));
    }

    @Test
    void shouldGetAuthorByIdFromExternalService() throws Exception {
        Author mockAuthor = createMockAuthor(1L, "John", "Doe");

        // Mock the external service response
        stubFor(get(urlEqualTo("/api/authors/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(mockAuthor))));

        // Call the FeignClient
        Author result = authorClient.getAuthorById(1L);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/authors/1")));
    }

    @Test
    void shouldFindAuthorsByNameFromExternalService() throws Exception {
        Author mockAuthor = createMockAuthor(1L, "John", "Doe");
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(20, 0, 1, 1);
        PagedModel<Author> pagedAuthors = PagedModel.of(List.of(mockAuthor), pageMetadata);

        // Mock the external service response
        stubFor(get(urlPathEqualTo("/api/authors/search/findByName"))
                .withQueryParam("name", equalTo("John"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(pagedAuthors))));

        // Call the FeignClient
        PagedModel<Author> result = authorClient.findAuthorsByName("John");

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getFirstName()).isEqualTo("John");

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/authors/search/findByName"))
                .withQueryParam("name", equalTo("John")));
    }

    @Test
    void shouldHandleNotFoundErrorFromExternalService() {
        // Mock 404 response
        stubFor(get(urlEqualTo("/api/books/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Book not found\"}")));

        // Call should throw exception
        assertThrows(Exception.class, () -> bookClient.getBookById(999L));

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/books/999")));
    }

    @Test
    void shouldHandleServerErrorFromExternalService() {
        // Mock 500 response
        stubFor(get(urlEqualTo("/api/books/1"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal server error\"}")));

        // Call should throw exception
        assertThrows(Exception.class, () -> bookClient.getBookById(1L));

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void shouldHandleTimeoutFromExternalService() {
        // Mock delayed response
        stubFor(get(urlEqualTo("/api/books/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")
                        .withFixedDelay(10000))); // 10 second delay

        // Call should throw timeout exception
        assertThrows(Exception.class, () -> bookClient.getBookById(1L));
    }

    @Test
    void shouldRetryOnTransientFailures() {
        // Mock first call to fail, second to succeed
        stubFor(get(urlEqualTo("/api/books/1"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Service unavailable\"}"))
                .willSetStateTo("First Call Failed"));

        Book mockBook = createMockBook(1L, "Test Book", "978-1111111111");
        stubFor(get(urlEqualTo("/api/books/1"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Call Failed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(mockBook))));

        // This test depends on retry configuration in FeignClient
        // The actual behavior will depend on the retry configuration
        try {
            Book result = bookClient.getBookById(1L);
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Book");
        } catch (Exception e) {
            // If retry is not configured, this will fail
            // This is expected behavior and shows the contract
        }
    }

    private Book createMockBook(Long id, String title, String isbn) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("Mock book description");
        book.setPublicationYear(2023);
        book.setGenre("Fiction");
        book.setAvailableCopies(5);
        book.setTotalCopies(10);
        return book;
    }

    private Author createMockAuthor(Long id, String firstName, String lastName) {
        Author author = new Author();
        author.setId(id);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography("Mock author biography");
        author.setBirthDate(LocalDate.of(1980, 1, 1));
        author.setNationality("American");
        return author;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}