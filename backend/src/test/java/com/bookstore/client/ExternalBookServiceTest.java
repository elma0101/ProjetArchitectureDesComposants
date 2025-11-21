package com.bookstore.client;

import com.bookstore.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.PagedModel;

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
class ExternalBookServiceTest {

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private ExternalBookService externalBookService;

    private Book testBook;
    private PagedModel<Book> pagedBooks;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        
        pagedBooks = PagedModel.of(Set.of(testBook), new PagedModel.PageMetadata(20, 0, 1));
    }

    @Test
    void shouldGetAllBooksSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(bookClient.getAllBooks(anyInt(), anyInt())).thenReturn(pagedBooks);

        // When
        CompletableFuture<PagedModel<Book>> result = externalBookService.getAllBooks(0, 20);

        // Then
        assertThat(result).isNotNull();
        PagedModel<Book> books = result.get();
        assertThat(books.getContent()).hasSize(1);
        assertThat(books.getContent().iterator().next().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldGetBookByIdSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(bookClient.getBookById(anyLong())).thenReturn(testBook);

        // When
        CompletableFuture<Book> result = externalBookService.getBookById(1L);

        // Then
        assertThat(result).isNotNull();
        Book book = result.get();
        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldFindBooksByTitleSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(bookClient.findBooksByTitle(anyString())).thenReturn(pagedBooks);

        // When
        CompletableFuture<PagedModel<Book>> result = externalBookService.findBooksByTitle("Test Book");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Book> books = result.get();
        assertThat(books.getContent()).hasSize(1);
    }

    @Test
    void shouldFindBooksByAuthorSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(bookClient.findBooksByAuthor(anyString())).thenReturn(pagedBooks);

        // When
        CompletableFuture<PagedModel<Book>> result = externalBookService.findBooksByAuthor("Test Author");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Book> books = result.get();
        assertThat(books.getContent()).hasSize(1);
    }

    @Test
    void shouldFindBooksByIsbnSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        when(bookClient.findBooksByIsbn(anyString())).thenReturn(pagedBooks);

        // When
        CompletableFuture<PagedModel<Book>> result = externalBookService.findBooksByIsbn("978-0123456789");

        // Then
        assertThat(result).isNotNull();
        PagedModel<Book> books = result.get();
        assertThat(books.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnFallbackWhenClientFails() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<PagedModel<Book>> fallbackResult = externalBookService.fallbackGetAllBooks(0, 20, new RuntimeException("Service unavailable"));

        // Then
        assertThat(fallbackResult).isNotNull();
        PagedModel<Book> books = fallbackResult.get();
        assertThat(books.getContent()).isEmpty();
    }

    @Test
    void shouldReturnNullInFallbackForSingleBook() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<Book> fallbackResult = externalBookService.fallbackGetBookById(1L, new RuntimeException("Service unavailable"));

        // Then
        assertThat(fallbackResult).isNotNull();
        Book book = fallbackResult.get();
        assertThat(book).isNull();
    }
}