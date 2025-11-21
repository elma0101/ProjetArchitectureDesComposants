package com.bookstore.client;

import com.bookstore.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.PagedModel;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAuthorServiceTest {

    @Mock
    private AuthorClient authorClient;

    @InjectMocks
    private ExternalAuthorService externalAuthorService;

    private Author testAuthor;
    private PagedModel<Author> pagedAuthors;

    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBirthDate(LocalDate.of(1980, 1, 1));
        testAuthor.setNationality("American");
        
        pagedAuthors = PagedModel.of(Set.of(testAuthor), new PagedModel.PageMetadata(20, 0, 1));
    }

    @Test
    void shouldGetAllAuthorsSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(authorClient.getAllAuthors(anyInt(), anyInt())).thenReturn(pagedAuthors);

        // When
        CompletableFuture<PagedModel<Author>> result = externalAuthorService.getAllAuthors(0, 20);

        // Then
        assertThat(result).isNotNull();
        PagedModel<Author> authors = result.get();
        assertThat(authors.getContent()).hasSize(1);
        assertThat(authors.getContent().iterator().next().getFirstName()).isEqualTo("John");
        assertThat(authors.getContent().iterator().next().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldGetAuthorByIdSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(authorClient.getAuthorById(anyLong())).thenReturn(testAuthor);

        // When
        CompletableFuture<Author> result = externalAuthorService.getAuthorById(1L);

        // Then
        assertThat(result).isNotNull();
        Author author = result.get();
        assertThat(author.getId()).isEqualTo(1L);
        assertThat(author.getFirstName()).isEqualTo("John");
        assertThat(author.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldFindAuthorsByNameSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(authorClient.findAuthorsByName(anyString())).thenReturn(pagedAuthors);

        // When
        CompletableFuture<PagedModel<Author>> result = externalAuthorService.findAuthorsByName("John Doe");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Author> authors = result.get();
        assertThat(authors.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAuthorsByFirstNameSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(authorClient.findAuthorsByFirstName(anyString())).thenReturn(pagedAuthors);

        // When
        CompletableFuture<PagedModel<Author>> result = externalAuthorService.findAuthorsByFirstName("John");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Author> authors = result.get();
        assertThat(authors.getContent()).hasSize(1);
    }

    @Test
    void shouldFindAuthorsByLastNameSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(authorClient.findAuthorsByLastName(anyString())).thenReturn(pagedAuthors);

        // When
        CompletableFuture<PagedModel<Author>> result = externalAuthorService.findAuthorsByLastName("Doe");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Author> authors = result.get();
        assertThat(authors.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnFallbackWhenClientFails() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<PagedModel<Author>> fallbackResult = externalAuthorService.fallbackGetAllAuthors(0, 20, new RuntimeException("Service unavailable"));

        // Then
        assertThat(fallbackResult).isNotNull();
        PagedModel<Author> authors = fallbackResult.get();
        assertThat(authors.getContent()).isEmpty();
    }

    @Test
    void shouldReturnNullInFallbackForSingleAuthor() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<Author> fallbackResult = externalAuthorService.fallbackGetAuthorById(1L, new RuntimeException("Service unavailable"));

        // Then
        assertThat(fallbackResult).isNotNull();
        Author author = fallbackResult.get();
        assertThat(author).isNull();
    }
}