package com.bookstore.controller;

import com.bookstore.client.ExternalAuthorService;
import com.bookstore.client.ExternalBookService;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalServiceController.class)
@Import(com.bookstore.config.TestSecurityConfig.class)
class ExternalServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalBookService externalBookService;

    @MockBean
    private ExternalAuthorService externalAuthorService;

    private Book testBook;
    private Author testAuthor;
    private PagedModel<Book> pagedBooks;
    private PagedModel<Author> pagedAuthors;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setGenre("Fiction");

        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBirthDate(LocalDate.of(1980, 1, 1));

        pagedBooks = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
        pagedAuthors = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
    }

    @Test
    void shouldGetExternalBooks() throws Exception {
        // Given
        when(externalBookService.getAllBooks(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(pagedBooks));

        // When & Then
        mockMvc.perform(get("/api/external/books")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Test Book"));
    }

    @Test
    void shouldGetExternalBookById() throws Exception {
        // Given
        when(externalBookService.getBookById(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(testBook));

        // When & Then
        mockMvc.perform(get("/api/external/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-0123456789"));
    }

    @Test
    void shouldSearchExternalBooks() throws Exception {
        // Given
        when(externalBookService.findBooksByTitle(anyString()))
                .thenReturn(CompletableFuture.completedFuture(pagedBooks));

        // When & Then
        mockMvc.perform(get("/api/external/books/search")
                        .param("title", "Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Test Book"));
    }

    @Test
    void shouldGetExternalAuthors() throws Exception {
        // Given
        when(externalAuthorService.getAllAuthors(anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(pagedAuthors));

        // When & Then
        mockMvc.perform(get("/api/external/authors")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("John"))
                .andExpect(jsonPath("$._embedded.authors[0].lastName").value("Doe"));
    }

    @Test
    void shouldGetExternalAuthorById() throws Exception {
        // Given
        when(externalAuthorService.getAuthorById(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(testAuthor));

        // When & Then
        mockMvc.perform(get("/api/external/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldSearchExternalAuthors() throws Exception {
        // Given
        when(externalAuthorService.findAuthorsByName(anyString()))
                .thenReturn(CompletableFuture.completedFuture(pagedAuthors));

        // When & Then
        mockMvc.perform(get("/api/external/authors/search")
                        .param("name", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("John"));
    }

    @Test
    void shouldHandleBookNotFound() throws Exception {
        // Given
        when(externalBookService.getBookById(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(get("/api/external/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleAuthorNotFound() throws Exception {
        // Given
        when(externalAuthorService.getAuthorById(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(get("/api/external/authors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        // Given
        CompletableFuture<Book> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Service unavailable"));
        when(externalBookService.getBookById(anyLong())).thenReturn(failedFuture);

        // When & Then
        mockMvc.perform(get("/api/external/books/1"))
                .andExpect(status().isInternalServerError());
    }
}