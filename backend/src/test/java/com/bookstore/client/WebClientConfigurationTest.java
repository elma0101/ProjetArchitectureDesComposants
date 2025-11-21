package com.bookstore.client;

import com.bookstore.config.WebClientConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for WebClient configuration and basic functionality
 */
class WebClientConfigurationTest {

    private MockWebServer mockWebServer;
    private WebClient webClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void webClient_ShouldMakeSuccessfulGetRequest() {
        // Given
        Map<String, Object> responseBody = Map.of(
                "id", 1,
                "title", "Test Book",
                "author", "Test Author"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"title\":\"Test Book\",\"author\":\"Test Author\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri("/api/books/1")
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .assertNext(result -> {
            assertThat(result).containsEntry("id", 1);
            assertThat(result).containsEntry("title", "Test Book");
            assertThat(result).containsEntry("author", "Test Author");
        })
        .verifyComplete();
    }

    @Test
    void webClient_ShouldMakeSuccessfulPostRequest() {
        // Given
        Map<String, Object> requestBody = Map.of(
                "title", "New Book",
                "author", "New Author"
        );
        
        Map<String, Object> responseBody = Map.of(
                "id", 2,
                "title", "New Book",
                "author", "New Author"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":2,\"title\":\"New Book\",\"author\":\"New Author\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(201));

        // When & Then
        StepVerifier.create(
                webClient.post()
                        .uri("/api/books")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .assertNext(result -> {
            assertThat(result).containsEntry("id", 2);
            assertThat(result).containsEntry("title", "New Book");
            assertThat(result).containsEntry("author", "New Author");
        })
        .verifyComplete();
    }

    @Test
    void webClient_ShouldHandleNotFoundError() {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri("/api/books/999")
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .expectError(WebClientResponseException.NotFound.class)
        .verify();
    }

    @Test
    void webClient_ShouldHandleServerError() {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri("/api/books")
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .expectError(WebClientResponseException.InternalServerError.class)
        .verify();
    }

    @Test
    void webClient_ShouldMakePutRequest() {
        // Given
        Map<String, Object> requestBody = Map.of(
                "id", 1,
                "title", "Updated Book",
                "author", "Updated Author"
        );
        
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"title\":\"Updated Book\",\"author\":\"Updated Author\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // When & Then
        StepVerifier.create(
                webClient.put()
                        .uri("/api/books/1")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .assertNext(result -> {
            assertThat(result).containsEntry("id", 1);
            assertThat(result).containsEntry("title", "Updated Book");
            assertThat(result).containsEntry("author", "Updated Author");
        })
        .verifyComplete();
    }

    @Test
    void webClient_ShouldMakeDeleteRequest() {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        // When & Then
        StepVerifier.create(
                webClient.delete()
                        .uri("/api/books/1")
                        .retrieve()
                        .bodyToMono(Void.class)
        )
        .verifyComplete();
    }

    @Test
    void webClient_ShouldHandleQueryParameters() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"results\":[],\"total\":0}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/books/search")
                                .queryParam("title", "Java")
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .build())
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .assertNext(result -> {
            assertThat(result).containsKey("results");
            assertThat(result).containsKey("total");
        })
        .verifyComplete();
    }

    @Test
    void webClient_ShouldHandleCustomHeaders() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message\":\"success\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri("/api/books")
                        .header("X-Custom-Header", "custom-value")
                        .header("Authorization", "Bearer token123")
                        .retrieve()
                        .bodyToMono(Map.class)
        )
        .assertNext(result -> {
            assertThat(result).containsEntry("message", "success");
        })
        .verifyComplete();
    }

    @Test
    void webClientConfig_ShouldCreateConfiguredWebClient() {
        // Given
        WebClientConfig config = new WebClientConfig();
        
        // When
        WebClient bookWebClient = config.bookServiceWebClient();
        WebClient authorWebClient = config.authorServiceWebClient();
        WebClient genericWebClient = config.genericWebClient();
        
        // Then
        assertThat(bookWebClient).isNotNull();
        assertThat(authorWebClient).isNotNull();
        assertThat(genericWebClient).isNotNull();
    }

    @Test
    void webClient_ShouldHandleErrorResponseWithBody() {
        // Given
        String errorBody = "{\"error\":\"Book not found\",\"code\":\"BOOK_NOT_FOUND\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(errorBody)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // When & Then
        StepVerifier.create(
                webClient.get()
                        .uri("/api/books/999")
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(), 
                                response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Client error: " + body)))
                        .bodyToMono(Map.class)
        )
        .expectErrorMatches(throwable -> 
                throwable instanceof RuntimeException && 
                throwable.getMessage().contains("Client error") &&
                throwable.getMessage().contains("Book not found"))
        .verify();
    }
}