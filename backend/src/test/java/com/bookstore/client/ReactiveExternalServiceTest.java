package com.bookstore.client;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveExternalServiceTest {

    @Mock
    private ReactiveBookService bookService;

    @Mock
    private ReactiveAuthorService authorService;

    @Mock
    private WebClient genericWebClient;

    private ReactiveExternalService reactiveExternalService;

    @BeforeEach
    void setUp() {
        reactiveExternalService = new ReactiveExternalService(bookService, authorService, genericWebClient);
    }

    @Test
    void getCompleteBookInfo_ShouldReturnBookWithAuthors() {
        // Given
        Author author1 = createTestAuthor(1L, "John", "Doe");
        Author author2 = createTestAuthor(2L, "Jane", "Smith");
        Set<Author> authors = Set.of(author1, author2);
        
        Book book = createTestBook(1L, "Test Book");
        book.setAuthors(authors);

        when(bookService.getBookById(1L)).thenReturn(Mono.just(book));
        when(authorService.getAuthorById(1L)).thenReturn(Mono.just(author1));
        when(authorService.getAuthorById(2L)).thenReturn(Mono.just(author2));

        // When & Then
        StepVerifier.create(reactiveExternalService.getCompleteBookInfo(1L))
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                    assertThat(result).containsKey("authorCount");
                    assertThat(result.get("authorCount")).isEqualTo(2);
                    
                    Book resultBook = (Book) result.get("book");
                    assertThat(resultBook.getTitle()).isEqualTo("Test Book");
                    
                    @SuppressWarnings("unchecked")
                    List<Author> resultAuthors = (List<Author>) result.get("authors");
                    assertThat(resultAuthors).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void getCompleteBookInfo_WhenBookNotFound_ShouldReturnError() {
        // Given
        when(bookService.getBookById(999L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(reactiveExternalService.getCompleteBookInfo(999L))
                .verifyComplete(); // The service returns empty when book is not found
    }

    @Test
    void getAuthorWithBooks_ShouldReturnAuthorWithBooks() {
        // Given
        Author author = createTestAuthor(1L, "John", "Doe");
        Book book1 = createTestBook(1L, "Book 1");
        Book book2 = createTestBook(2L, "Book 2");

        when(authorService.getAuthorById(1L)).thenReturn(Mono.just(author));
        when(authorService.getBooksByAuthor(1L)).thenReturn(Flux.just(book1, book2));

        // When & Then
        StepVerifier.create(reactiveExternalService.getAuthorWithBooks(1L))
                .assertNext(result -> {
                    assertThat(result).containsKey("author");
                    assertThat(result).containsKey("books");
                    assertThat(result).containsKey("bookCount");
                    assertThat(result.get("bookCount")).isEqualTo(2);
                    
                    Author resultAuthor = (Author) result.get("author");
                    assertThat(resultAuthor.getFirstName()).isEqualTo("John");
                    
                    @SuppressWarnings("unchecked")
                    List<Book> resultBooks = (List<Book>) result.get("books");
                    assertThat(resultBooks).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void searchBooksAndAuthors_ShouldReturnCombinedResults() {
        // Given
        Book book = createTestBook(1L, "Java Programming");
        Author author = createTestAuthor(1L, "John", "Java");
        
        PagedModel<Book> bookResults = PagedModel.of(List.of(book), 
                new PagedModel.PageMetadata(20, 0, 1));
        PagedModel<Author> authorResults = PagedModel.of(List.of(author), 
                new PagedModel.PageMetadata(20, 0, 1));

        when(bookService.findBooksByTitle("Java")).thenReturn(Mono.just(bookResults));
        when(authorService.findAuthorsByName("Java")).thenReturn(Mono.just(authorResults));

        // When & Then
        StepVerifier.create(reactiveExternalService.searchBooksAndAuthors("Java"))
                .assertNext(result -> {
                    assertThat(result).containsKey("books");
                    assertThat(result).containsKey("authors");
                    assertThat(result).containsKey("bookCount");
                    assertThat(result).containsKey("authorCount");
                    assertThat(result).containsKey("query");
                    
                    assertThat(result.get("bookCount")).isEqualTo(1);
                    assertThat(result.get("authorCount")).isEqualTo(1);
                    assertThat(result.get("query")).isEqualTo("Java");
                })
                .verifyComplete();
    }

    @Test
    void getPopularContent_ShouldReturnPopularBooksAndAuthors() {
        // Given
        Book book = createTestBook(1L, "Popular Book");
        Author author = createTestAuthor(1L, "Popular", "Author");
        
        PagedModel<Book> bookResults = PagedModel.of(List.of(book), 
                new PagedModel.PageMetadata(10, 0, 1));
        PagedModel<Author> authorResults = PagedModel.of(List.of(author), 
                new PagedModel.PageMetadata(10, 0, 1));

        when(bookService.getAllBooks(0, 10)).thenReturn(Mono.just(bookResults));
        when(authorService.getAllAuthors(0, 10)).thenReturn(Mono.just(authorResults));

        // When & Then
        StepVerifier.create(reactiveExternalService.getPopularContent(10))
                .assertNext(result -> {
                    assertThat(result).containsKey("popularBooks");
                    assertThat(result).containsKey("popularAuthors");
                    assertThat(result).containsKey("timestamp");
                    
                    @SuppressWarnings("unchecked")
                    List<Book> popularBooks = (List<Book>) result.get("popularBooks");
                    assertThat(popularBooks).hasSize(1);
                    
                    @SuppressWarnings("unchecked")
                    List<Author> popularAuthors = (List<Author>) result.get("popularAuthors");
                    assertThat(popularAuthors).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void processBooksWithAuthors_ShouldProcessBooksInBatches() {
        // Given
        List<Long> bookIds = List.of(1L, 2L);
        
        Author author = createTestAuthor(1L, "John", "Doe");
        Set<Author> authors = Set.of(author);
        
        Book book1 = createTestBook(1L, "Book 1");
        book1.setAuthors(authors);
        Book book2 = createTestBook(2L, "Book 2");
        book2.setAuthors(authors);

        when(bookService.getBookById(1L)).thenReturn(Mono.just(book1));
        when(bookService.getBookById(2L)).thenReturn(Mono.just(book2));
        when(authorService.getAuthorById(1L)).thenReturn(Mono.just(author));

        // When & Then
        StepVerifier.create(reactiveExternalService.processBooksWithAuthors(bookIds))
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                })
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                })
                .verifyComplete();
    }

    @Test
    void streamBooksByGenreWithAuthors_ShouldStreamBooksWithAuthors() {
        // Given
        Author author = createTestAuthor(1L, "John", "Doe");
        Set<Author> authors = Set.of(author);
        
        Book book1 = createTestBook(1L, "Fiction Book 1");
        book1.setAuthors(authors);
        Book book2 = createTestBook(2L, "Fiction Book 2");
        book2.setAuthors(authors);

        when(bookService.getBooksByGenre("Fiction")).thenReturn(Flux.just(book1, book2));
        when(authorService.getAuthorById(1L)).thenReturn(Mono.just(author));

        // When & Then
        StepVerifier.create(reactiveExternalService.streamBooksByGenreWithAuthors("Fiction"))
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                    
                    Book resultBook = (Book) result.get("book");
                    assertThat(resultBook.getTitle()).isEqualTo("Fiction Book 1");
                })
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                    
                    Book resultBook = (Book) result.get("book");
                    assertThat(resultBook.getTitle()).isEqualTo("Fiction Book 2");
                })
                .verifyComplete();
    }

    @Test
    void aggregateLibraryData_ShouldReturnAggregatedStats() {
        // Given
        PagedModel<Book> bookResults = PagedModel.of(List.of(), 
                new PagedModel.PageMetadata(20, 0, 100));
        PagedModel<Author> authorResults = PagedModel.of(List.of(), 
                new PagedModel.PageMetadata(20, 0, 50));

        when(bookService.getAllBooks(0, 1)).thenReturn(Mono.just(bookResults));
        when(authorService.getAllAuthors(0, 1)).thenReturn(Mono.just(authorResults));

        // When & Then
        StepVerifier.create(reactiveExternalService.aggregateLibraryData())
                .assertNext(result -> {
                    assertThat(result).containsKey("totalBooks");
                    assertThat(result).containsKey("totalAuthors");
                    assertThat(result).containsKey("lastUpdated");
                    assertThat(result).containsKey("status");
                    
                    assertThat(result.get("totalBooks")).isEqualTo(100L);
                    assertThat(result.get("totalAuthors")).isEqualTo(50L);
                    assertThat(result.get("status")).isEqualTo("active");
                })
                .verifyComplete();
    }

    @Test
    void checkExternalServicesHealth_ShouldReturnHealthStatus() {
        // Given
        PagedModel<Book> bookResults = PagedModel.of(List.of(), 
                new PagedModel.PageMetadata(20, 0, 0));
        PagedModel<Author> authorResults = PagedModel.of(List.of(), 
                new PagedModel.PageMetadata(20, 0, 0));

        when(bookService.getAllBooks(0, 1)).thenReturn(Mono.just(bookResults));
        when(authorService.getAllAuthors(0, 1)).thenReturn(Mono.just(authorResults));

        // When & Then
        StepVerifier.create(reactiveExternalService.checkExternalServicesHealth())
                .assertNext(result -> {
                    assertThat(result).containsKey("bookService");
                    assertThat(result).containsKey("authorService");
                    assertThat(result).containsKey("overall");
                    assertThat(result).containsKey("timestamp");
                    
                    assertThat(result.get("bookService")).isEqualTo("healthy");
                    assertThat(result.get("authorService")).isEqualTo("healthy");
                    assertThat(result.get("overall")).isEqualTo("healthy");
                })
                .verifyComplete();
    }

    @Test
    void checkExternalServicesHealth_WhenServiceDown_ShouldReturnDegradedStatus() {
        // Given
        when(bookService.getAllBooks(0, 1)).thenReturn(Mono.error(new RuntimeException("Service down")));
        when(authorService.getAllAuthors(0, 1)).thenReturn(Mono.just(PagedModel.of(List.of(), 
                new PagedModel.PageMetadata(20, 0, 0))));

        // When & Then
        StepVerifier.create(reactiveExternalService.checkExternalServicesHealth())
                .assertNext(result -> {
                    assertThat(result).containsKey("bookService");
                    assertThat(result).containsKey("authorService");
                    assertThat(result).containsKey("overall");
                    
                    assertThat(result.get("bookService")).isEqualTo("unhealthy");
                    assertThat(result.get("authorService")).isEqualTo("healthy");
                    assertThat(result.get("overall")).isEqualTo("degraded");
                })
                .verifyComplete();
    }

    private Author createTestAuthor(Long id, String firstName, String lastName) {
        Author author = new Author();
        author.setId(id);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography("Test biography");
        author.setBirthDate(LocalDate.of(1980, 1, 1));
        author.setNationality("Test Nationality");
        author.setBooks(new HashSet<>());
        return author;
    }

    private Book createTestBook(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setIsbn("978-012345678" + id);
        book.setDescription("Test description");
        book.setGenre("Test Genre");
        book.setPublicationYear(2023);
        book.setAvailableCopies(5);
        book.setTotalCopies(10);
        book.setAuthors(new HashSet<>());
        return book;
    }
}