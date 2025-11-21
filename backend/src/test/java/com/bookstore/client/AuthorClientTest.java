package com.bookstore.client;

import com.bookstore.entity.Author;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "bookstore.external.author-service.url=http://localhost:${wiremock.server.port}"
})
class AuthorClientTest {

    @Autowired
    private AuthorClient authorClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBiography("A test author");
        testAuthor.setBirthDate(LocalDate.of(1980, 1, 1));
        testAuthor.setNationality("American");

    }

    @Test
    void shouldGetAllAuthors() throws Exception {
        // Given
        PagedModel<Author> expectedResponse = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors?page=0&size=20"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Author> result = authorClient.getAllAuthors(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getFirstName()).isEqualTo("John");
        assertThat(result.getContent().iterator().next().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldGetAuthorById() throws Exception {
        // Given
        stubFor(get(urlEqualTo("/api/authors/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(testAuthor))));

        // When
        Author result = authorClient.getAuthorById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getNationality()).isEqualTo("American");
    }

    @Test
    void shouldFindAuthorsByName() throws Exception {
        // Given
        PagedModel<Author> expectedResponse = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors/search/findByName?name=John%20Doe"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Author> result = authorClient.findAuthorsByName("John Doe");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getFirstName()).isEqualTo("John");
        assertThat(result.getContent().iterator().next().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldFindAuthorsByFirstName() throws Exception {
        // Given
        PagedModel<Author> expectedResponse = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors/search/findByFirstName?firstName=John"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Author> result = authorClient.findAuthorsByFirstName("John");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindAuthorsByLastName() throws Exception {
        // Given
        PagedModel<Author> expectedResponse = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
        
        stubFor(get(urlEqualTo("/api/authors/search/findByLastName?lastName=Doe"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(expectedResponse))));

        // When
        PagedModel<Author> result = authorClient.findAuthorsByLastName("Doe");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().iterator().next().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldHandleNotFoundError() {
        // Given
        stubFor(get(urlEqualTo("/api/authors/999"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Author not found\"}")));

        // When & Then
        assertThrows(Exception.class, () -> authorClient.getAuthorById(999L));
    }

    @Test
    void shouldHandleServerError() {
        // Given
        stubFor(get(urlEqualTo("/api/authors/1"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal server error\"}")));

        // When & Then
        assertThrows(Exception.class, () -> authorClient.getAuthorById(1L));
    }
}