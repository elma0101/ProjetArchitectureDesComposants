package com.bookstore.client;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "bookstore.external.book-service.url=http://localhost:${wiremock.server.port}"
})
class BookClientTest {

    @Autowired
    private BookClient bookClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setDescription("A test book");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setAvailableCopies(5);
        testBook.setTotalCopies(10);

    }

    @Test
    void shouldGetAllBooks() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books?page=0&size=20"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.getAllBooks(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldGetBookById() throws Exception {
        // Given
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(testBook))));

        // When
        Book result = bookClient.getBookById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getIsbn()).isEqualTo("978-0123456789");
    }

    @Test
    void shouldFindBooksByTitle() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books/search/findByTitle?title=Test%20Book"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.findBooksByTitle("Test Book");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldFindBooksByAuthor() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books/search/findByAuthor?author=Test%20Author"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.findBooksByAuthor("Test Author");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindBooksByIsbn() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books/search/findByIsbn?isbn=978-0123456789"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.findBooksByIsbn("978-0123456789");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getIsbn()).isEqualTo("978-0123456789");
    }

    @Test
    void shouldFindBooksByGenre() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/books/search/findByGenre?genre=Fiction"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.findBooksByGenre("Fiction");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getGenre()).isEqualTo("Fiction");
    }

    @Test
    void shouldGetBooksByAuthor() throws Exception {
        // Given
        PagedModel<Book> expectedResponse = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors/1/books"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Book> result = bookClient.getBooksByAuthor(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldHandleNotFoundError() {
        // Given
        stubFor(get(urlEqualTo("/api/books/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Book not found\"}")));

        // When & Then
        assertThrows(Exception.class, () -> bookClient.getBookById(999L));
    }

    @Test
    void shouldHandleServerError() {
        // Given
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal server error\"}")));

        // When & Then
        assertThrows(Exception.class, () -> bookClient.getBookById(1L));
    }
}